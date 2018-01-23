package uk.co.greenjam.jrgfinance.core.services.impl;

import com.adobe.fd.fp.common.PortalUtilsComponent;
import com.adobe.fd.fp.exception.FormsPortalException;
import com.adobe.fd.fp.model.PendingSignMetadata;
import com.adobe.fd.fp.service.*;
import com.adobe.fd.fp.service.Statement.Operator;
import com.adobe.granite.resourceresolverhelper.ResourceResolverHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;
import uk.co.greenjam.jrgfinance.core.config.FormsPortalMetadataServiceImplConfiguration;
import uk.co.greenjam.jrgfinance.core.utils.JRGFinanceFormsPortalConstants;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component(
        immediate = true,
        name = "Forms Portal Sample Metadata service Impl",
        service = {SubmitMetadataService.class, DraftMetadataService.class, PendingSignMetadataService.class},
        property = {
                "aem.formsportal.impl.prop=jrgfinance.metadataservice"
        }
)
@Designate(ocd = FormsPortalMetadataServiceImplConfiguration.class)
public class FormsPortalMetadataServiceImpl implements SubmitMetadataService, DraftMetadataService, PendingSignMetadataService {

    @Reference
    private PortalUtilsComponent portalUtilsComponent;

    @Reference
    protected  SlingRepository slingRepository;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    private volatile FPKeyGeneratorService fpKeyGeneratorService;

    @Reference
    private ResourceResolverHelper resourceResolverHelper;

    private static final List<String> multiValuedProps = new ArrayList<String>();

    private static final Map<Operator, String> sqlOperatorMap = new HashMap<Operator, String>();

    static {
        multiValuedProps.add(JRGFinanceFormsPortalConstants.STR_ATTACHMENT_LIST);
        multiValuedProps.add(JRGFinanceFormsPortalConstants.STR_NEXT_SIGNERS);

        sqlOperatorMap.put(Operator.EQUALS, "=");
        sqlOperatorMap.put(Operator.NOT_EQUALS, "<>");
        sqlOperatorMap.put(Operator.LIKE, "LIKE");
        sqlOperatorMap.put(Operator.NOT, "NOT");
        sqlOperatorMap.put(Operator.EXISTS, "IS NOT NULL");
    }

    private String dataSource;
    private String metadataTable;
    private String additionalMetadataTable;
    private String commentTable;
    private BundleContext bundleContext;

    @Activate
    @Modified
    protected void activate(final FormsPortalMetadataServiceImplConfiguration config){
        dataSource                  = config.getDataSource();
        metadataTable               = config.getMetadataTable();
        additionalMetadataTable     = config.getAdditionalMetadataTable();
        commentTable                = config.getCommentTable();
        bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
    }

    private String getDataSourceName(){
        return dataSource;
    }

    private String getMetadataTableName(){
        return metadataTable;
    }

    private String getAdditionalMetadataTableName(){
        return additionalMetadataTable;
    }

    /**
     *
     * @return a connection using the configured DataSourcePool
     * @throws FormsPortalException
     */
    private Connection getConnection() throws FormsPortalException{
        try {
            String filter = "(&(objectclass=javax.sql.DataSource)(datasource.name=" + getDataSourceName() + "))";
            ServiceReference[] refs = bundleContext.getAllServiceReferences(null, filter);
            if (refs != null && refs.length == 1) {
                DataSource dataSource = (DataSource) bundleContext.getService(refs[0]);
                return dataSource.getConnection();
            }
            throw new FormsPortalException();
        } catch (Exception e) {
            throw new FormsPortalException(e.getMessage(), e);
        }
    }

    private List<String> listAllColumns(Connection connection) throws FormsPortalException{
        try{
            List<String> columnsList = new ArrayList<String>();
            String getColumnsStmt = "SHOW COLUMNS FROM " + getMetadataTableName();
            PreparedStatement prStmt = connection.prepareStatement(getColumnsStmt);
            ResultSet resultSet = prStmt.executeQuery();
            while(resultSet.next()){
                String name = resultSet.getString("Field");
                columnsList.add(name);
            }
            return columnsList;
        } catch(Exception e){
            throw new FormsPortalException(e);
        }
    }

    /**
     * To save metadata associated with a draft
     * This method takes a map as argument. This map consists of metadata properties as key and the values corresponds to property's value
     * The mandatory key in this map for a draft is "draftID"
     * In order to manage all the metadata in single table, we are having one additional property which is primary key of the table- "id". We will assign draftID to id attribute
     * This method returns draftID associated with this draft. draftID denotes the metadata id associated with the draft
     * Need to take care of type of properties. Right now value is only of type String and String[]
     */
    public String saveMetadata(Map<String, Object> metadataMap) throws FormsPortalException {
        Connection connection = null;
        try{
            connection = getConnection();
            connection.setAutoCommit(false);

            String id = metadataMap.get(JRGFinanceFormsPortalConstants.STR_DRAFT_ID).toString();
            metadataMap.put(JRGFinanceFormsPortalConstants.STR_ID, id);
            insertMetadata(id, metadataMap, connection);

            /**
             * Committing after all the operations
             */
            connection.commit();
            return id;
        } catch(Exception e){
            try{
                /**
                 *  In case of any error, rollback
                 */
                if(connection!=null){
                    connection.rollback();
                }
            }catch(SQLException e2){
                throw new FormsPortalException(e2);
            }
            throw new FormsPortalException(e);
        } finally{
            try {
                /**
                 * Close the connection in finally block
                 */
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To save metadata associated with a submission
     * This method takes a map as argument. This map consists of metadata properties as key and the values corresponds to property's value
     * If a key "submitID" is not present, we need to create one and do further processing
     *  In order to manage all the metadata in single table, we are having one additional property which is primary key of the table- "id". We will assign submitID to id attribute
     *  This method returns the metadata object of submitted form in JSON format. For adaptive form, this object will also be used for redirect URL creation
     *  "submitID" is a must have key for this resultant object.
     *  Need to take care of type of properties. Right now value is only of type String and String[]
     */
    public JSONObject submitMetadata(Map<String, Object> metadataMap) throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            String id = null;
            if(metadataMap.containsKey(JRGFinanceFormsPortalConstants.STR_SUBMIT_ID)){
                id = metadataMap.get(JRGFinanceFormsPortalConstants.STR_SUBMIT_ID).toString();
            } else {
                id = fpKeyGeneratorService.getUniqueId();
                metadataMap.put(JRGFinanceFormsPortalConstants.STR_SUBMIT_ID, id);
            }
            metadataMap.put(JRGFinanceFormsPortalConstants.STR_ID, id);
            insertMetadata(id, metadataMap, connection);

            /**
             * Committing after all the operations
             */
            connection.commit();

            JSONObject submittedInstance = new JSONObject();
            String getSubmittedInstance = "SELECT * FROM " + getMetadataTableName() + " WHERE id = (?)";
            prStmt = connection.prepareStatement(getSubmittedInstance);
            prStmt.setString(1, id);
            ResultSet result = prStmt.executeQuery();
            if(result.next()){
                submittedInstance.put(JRGFinanceFormsPortalConstants.STR_SUBMIT_ID, result.getString(JRGFinanceFormsPortalConstants.STR_SUBMIT_ID));
                submittedInstance.put(JRGFinanceFormsPortalConstants.STR_FORM_NAME, result.getString(JRGFinanceFormsPortalConstants.STR_FORM_NAME));
                submittedInstance.put(JRGFinanceFormsPortalConstants.STR_OWNER, result.getString(JRGFinanceFormsPortalConstants.STR_OWNER));
                submittedInstance.put(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED, result.getString(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED));
            }
            if(result != null){
                result.close();
            }
            return submittedInstance;
        } catch(Exception e){
            try{
                /**
                 *  In case of any error, rollback
                 */
                if(connection!=null){
                    connection.rollback();
                }
            }catch(SQLException e2){
                throw new FormsPortalException(e2.getMessage(), e2);
            }
            throw new FormsPortalException(e);
        } finally{
            try {
                /**
                 * Close the statement and connection in finally block
                 */
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To list all the drafts associated with a user
     * This method will return as JSONArry of all the drafts where each draft is represented by a flat JSON object	 
     * This JSONArray is used for listing all the draft on the UI
     */
    public JSONArray getDrafts(String cutPoints) throws FormsPortalException {
        Connection connection = null;
        try{
            connection = getConnection();
            return listItems(JRGFinanceFormsPortalConstants.STR_FP_DRAFT, cutPoints, connection);
        } catch(Exception e){
            throw new FormsPortalException(e);
        } finally{
            try {
                /**
                 * Close the connection in finally block
                 */
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To list all the submissions associated with a user
     * This method will return as JSONArry of all the submissions where each submission is represented by a flat JSON object
     * This JSONArray is used for listing all the submissions on the UI	 
     */
    public JSONArray getSubmissions(String cutPoints) throws FormsPortalException {
        Connection connection = null;
        try{
            connection = getConnection();
            return listItems(JRGFinanceFormsPortalConstants.STR_FP_SUBMITTED_FORM, cutPoints, connection);
        } catch(Exception e){
            throw new FormsPortalException(e);
        } finally{
            try {
                /**
                 * Close the connection in finally block
                 */
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To delete metadata information associated with a draft or submission
     * This method uses logged-in user's information to verify whether this user is owner of this metadata or not to make sure he is the one authorized to delete
     * metadata id is provided to this method and the corresponding item is deleted
     * It returns the status of delete operation performed
     */
    public boolean deleteMetadata(String id) throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            String deleteStmt = "DELETE FROM " + getMetadataTableName() + " WHERE id = (?) ";
            prStmt = connection.prepareStatement(deleteStmt);
            prStmt.setString(1, id);
            prStmt.execute();

            connection.commit();
            return true;
        } catch (Exception e) {
            throw new FormsPortalException(e);
        } finally {
            try {
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To get a metadata property stored for a draft/submission instance
     * This method will take draft/submission metadata id and the propertyName as arguments
     * This will return a string array. 
     * If the property is single valued, it needs to return an array with only one element
     * If the property is multivalued, it needs to return an array with all the values
     * If the property does not exist, it is supposed to return an array with single empty value
     */
    public String[] getProperty(String id, String propertyName) throws FormsPortalException{
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            String result = "";
            List<String> columnsList = listAllColumns(connection);
            if(columnsList != null && columnsList.contains(propertyName)){
                String getPropertyStmt = "SELECT " + propertyName + " FROM " + getMetadataTableName() + " WHERE id = (?)";
                prStmt = connection.prepareStatement(getPropertyStmt);
                prStmt.setString(1, id);
                ResultSet resultSet = prStmt.executeQuery();
                if(resultSet != null && resultSet.next()){
                    if(resultSet.getString(propertyName) != null){
                        result = resultSet.getString(propertyName);
                    }
                    /**
                     * Special care for attachmentList only. This property can be multivalued and we need it in the same format that we had provided it while saving/submitting
                     */
                    if (multiValuedProps.contains(propertyName)) {
                        return deflateList(result);
                    }
                }
            } else{
                /**
                 * If property does not exist in metadata table, we need to search it in additional metadata table
                 */
                String additionalMetadataStr = "SELECT " + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_VALUE + " FROM " + getAdditionalMetadataTableName() + " WHERE " +
                        "`" + JRGFinanceFormsPortalConstants.STR_ID  + "` = (?) AND `" + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_KEY + "` = (?)";
                prStmt = connection.prepareStatement(additionalMetadataStr);
                prStmt.setString(1, id);
                prStmt.setString(2, propertyName);
                ResultSet resultSet = prStmt.executeQuery();
                if(resultSet.next()){
                    result = resultSet.getString(JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_VALUE);
                }
            }
            return new String[]{result};
        } catch (SQLException e) {
            return new String[]{""};
        } finally {
            try {
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * To delete a property associated with a draft/submission
     * This method will take draft/submission metadata id and queried property's Name
     * This method returns status of the delete operation 
     */
    public boolean deleteProperty(String id, String propertyName) throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            List<String> columnsList = listAllColumns(connection);
            String userId = resourceResolverHelper.getResourceResolver().getUserID();

            if(columnsList != null && columnsList.contains(propertyName)){
                String deletePropertyStmt = "UPDATE " + getMetadataTableName() + " SET (?) = NULL WHERE id = (?) AND owner = (?)";
                prStmt = connection.prepareStatement(deletePropertyStmt);
                prStmt.setString(1, propertyName);
                prStmt.setString(2, id);
                prStmt.setString(3, userId);
            } else {
                String deletePropertyStmt = "DELETE FROM " + getAdditionalMetadataTableName() + " WHERE " + JRGFinanceFormsPortalConstants.STR_ID + " = (?) AND `" + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_KEY + "` = (?)";
                prStmt = connection.prepareStatement(deletePropertyStmt);
                prStmt.setString(1, id);
                prStmt.setString(2, propertyName);
            }
            boolean result =  prStmt.execute();
            connection.commit();
            return result;
        } catch (Exception e) {
            throw new FormsPortalException(e);
        } finally {
            try {
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /*
     * TODO: This is common method for listing draft,submissions and pending Sign
     * Query can be improved as some of the conditions are not valid for others
     */
    private JSONArray listItems(String itemType, String cutPoints, Connection connection) throws FormsPortalException{
        PreparedStatement prStmt = null;
        ResultSet resultSet = null;
        try{
            String userId = resourceResolverHelper.getResourceResolver().getUserID();
            String getItemsStmt = "SELECT * FROM " + getMetadataTableName() + " LEFT JOIN " + getAdditionalMetadataTableName() +
                    " ON " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_ID + " = " + getAdditionalMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_ID +
                    " WHERE " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_OWNER + " = (?) " +
                    "AND " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_NODE_TYPE + " = (?) "+
                    "AND " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_USERDATA_ID + " IS NOT NULL "+
                    "AND (" + metadataTable + "." + JRGFinanceFormsPortalConstants.STR_MARKED_FOR_DELETION + " != 'true' OR " +
                    metadataTable + "." + JRGFinanceFormsPortalConstants.STR_MARKED_FOR_DELETION
                    + " IS NULL ) AND (" + JRGFinanceFormsPortalConstants.STR_OWNER + "!= '"+ JRGFinanceFormsPortalConstants.STR_ANONYMOUS_USER +"')" +
                    " order by `" + JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED + "` asc";

            prStmt = connection.prepareStatement(getItemsStmt);
            prStmt.setString(1, userId);
            prStmt.setString(2, itemType);
            return readItems(prStmt, itemType, cutPoints, connection);
        } catch(Exception e){
            throw new FormsPortalException(e);
        } finally{
            try {
                if(resultSet != null){
                    resultSet.close();
                }
                if(prStmt != null){
                    prStmt.close();
                }
            }catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    private JSONArray readItems(PreparedStatement prStmt, String itemType, String cutPoints, Connection connection) throws FormsPortalException {
        ResultSet resultSet = null;
        try{
            resultSet = prStmt.executeQuery();

            JSONArray items = new JSONArray();
            String oldId = null;
            JSONObject olditem = null;
            List<String> cutPointsList = Arrays.asList(cutPoints.split(","));
            while(resultSet.next()){
                String currentId = resultSet.getString(JRGFinanceFormsPortalConstants.STR_ID);
                if(oldId == null || !oldId.equals(currentId)){
                    // The existing (oldId) is not null i.e. an entry that already existed but is not the same as the current one, we need to add it to the array as we are done with this id's entries
                    if(oldId != null){
                        items.put(olditem);
                    }
                    olditem = new JSONObject();
                    oldId = currentId;
                    String additionalMetadataKey = resultSet.getString(JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_KEY);
                    String additionalMetadataVal = resultSet.getString(JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_VALUE);
                    for(String cutPoint : cutPointsList){
                        try{
                            if (cutPoint.equals(additionalMetadataKey)) {
                                olditem.put(additionalMetadataKey, additionalMetadataVal);
                            } else if (resultSet.getString(cutPoint) == null) {
                                olditem.put(cutPoint, "");
                            } else {
                                olditem.put(cutPoint, resultSet.getString(cutPoint));
                            }
                        } catch (SQLException e){
                            olditem.put(cutPoint, "");
                        }
                    }
                    if(cutPointsList.contains(JRGFinanceFormsPortalConstants.STR_NAME) && StringUtils.isEmpty(olditem.getString(JRGFinanceFormsPortalConstants.STR_NAME))) {
                        olditem.put(JRGFinanceFormsPortalConstants.STR_NAME, resultSet.getString(JRGFinanceFormsPortalConstants.STR_FORM_NAME));
                    }
                    olditem.put(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED, resultSet.getString(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED));
                } else {
                    String additionalMetadataKey = resultSet.getString(JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_KEY);
                    String additionalMetadataVal = resultSet.getString(JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_VALUE);
                    if(cutPointsList.contains(additionalMetadataKey)){
                        olditem.put(additionalMetadataKey, additionalMetadataVal);
                    }
                }
            }
            if(olditem != null){
                items.put(olditem);
            }
            if(resultSet != null){
                resultSet.close();
            }
            return items;
        } catch(Exception e){
            throw new FormsPortalException(e);
        } finally{
            try {
                if(resultSet != null){
                    resultSet.close();
                }
                if(prStmt != null){
                    prStmt.close();
                }
            }catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    private void insertMetadata(String id, Map<String, Object> metadataMap, Connection connection) throws FormsPortalException {
        PreparedStatement prStmtMetadataTable = null;
        PreparedStatement prStmtAdditionalMetadataTable = null;
        try {
            String insertStmt = "INSERT INTO " + getMetadataTableName();

            List<String> columnsListMain = listAllColumns(connection);
            List<String> metadataKeysMain = new ArrayList<String>(metadataMap.keySet());

            // columnsListAndMetadatakeys contains metadata keys that are common to metadata keys sent from client and columns present in the table
            // Update only those metadata keys which are requested to.
            List<String> columnsListAndMetadatakeys = new ArrayList<String>(columnsListMain);
            columnsListAndMetadatakeys.retainAll(metadataKeysMain);

            String columnsStr = StringUtils.join(columnsListAndMetadatakeys, "`,`");
            int columnsCount = columnsListAndMetadatakeys.size();

            StringBuffer stmtBuffer = new StringBuffer();
            stmtBuffer.append(insertStmt).append(" (" + "`" + columnsStr + "`" + ")");

            String placeholder = "(?), ";
            String valuesPlaceHolders = StringUtils.repeat(placeholder, columnsCount);
            valuesPlaceHolders = valuesPlaceHolders.substring(0, valuesPlaceHolders.length()-2);
            stmtBuffer.append(" VALUES(" + valuesPlaceHolders +")");

            String onDuplicateStmt = " ON DUPLICATE KEY UPDATE ";
            stmtBuffer.append(onDuplicateStmt);
            for(String column : columnsListAndMetadatakeys){
                stmtBuffer.append("`" + column + "`" + " = (?), ");
            }

            String statementString = stmtBuffer.toString();
            //Removing trailing "," and " "
            statementString = statementString.substring(0, statementString.length()-2);
            prStmtMetadataTable = connection.prepareStatement(statementString);

            int count = 1;
            for(String column : columnsListAndMetadatakeys){
                Object val = metadataMap.get(column);
                String strVal = null;
                if (val != null) {
                    if (List.class.isAssignableFrom(val.getClass())) {
                        strVal = flattentList((List<?>)val);
                    } else if (val.getClass().isArray()) {
                        strVal = flattenArray((Object[])val);
                    } else {
                        strVal = String.valueOf(val);
                    }
                }
                prStmtMetadataTable.setString(count, strVal);
                prStmtMetadataTable.setString(count + columnsCount, strVal);
                count++;
            }


            /** Prepare a statement for additional metadata to store them in a separate table
             **  Each metadata will represent a row here
             ** ______________________
             ** |  key |  value | id |
             ** |      |        |    |
             ** |______|________|____|
             **
             **/
            List<String> metadataKeysMinusColumnsList = new ArrayList<String>(metadataKeysMain);
            metadataKeysMinusColumnsList.removeAll(columnsListMain);
            if(metadataKeysMinusColumnsList.size() > 0){
                insertStmt = "INSERT INTO " + getAdditionalMetadataTableName() + " (`" + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_KEY + "` , `" +
                        JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_VALUE + "` , `" + JRGFinanceFormsPortalConstants.STR_ID + "`) VALUES ";

                //Reuse the variable declared above
                stmtBuffer.setLength(0);
                stmtBuffer.append(insertStmt);


                int rowsCount = metadataKeysMinusColumnsList.size();
                placeholder = "((?), (?), (?)) ";
                valuesPlaceHolders = StringUtils.repeat(placeholder, " , ", rowsCount);
                stmtBuffer.append(valuesPlaceHolders);
                stmtBuffer.append(onDuplicateStmt);

                String updateString = "`" + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_KEY + "`" + " = VALUES(`" + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_KEY + "`), " +
                        "`" + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_VALUE + "`" + " = VALUES(`" + JRGFinanceFormsPortalConstants.STR_ADDITIONALMETADATA_VALUE + "`), " +
                        "`" + JRGFinanceFormsPortalConstants.STR_ID + "`" + " = VALUES(`" + JRGFinanceFormsPortalConstants.STR_ID + "`)" ;

                stmtBuffer.append(updateString);
                statementString = stmtBuffer.toString();
                prStmtAdditionalMetadataTable = connection.prepareStatement(statementString);
                count = 1;

                for(String keyToInsert : metadataKeysMinusColumnsList){
                    prStmtAdditionalMetadataTable.setString(count++, keyToInsert);
                    String val = metadataMap.get(keyToInsert) != null ? metadataMap.get(keyToInsert).toString() : null;
                    prStmtAdditionalMetadataTable.setString(count++, val);
                    prStmtAdditionalMetadataTable.setString(count++, id);
                }
            }

            if(prStmtMetadataTable != null){
                prStmtMetadataTable.execute();
            }
            if(prStmtAdditionalMetadataTable != null){
                prStmtAdditionalMetadataTable.execute();
            }
        } catch (Exception e) {
            throw new FormsPortalException(e);
        } finally {
            try {
                if(prStmtMetadataTable != null){
                    prStmtMetadataTable.close();
                }
                if(prStmtAdditionalMetadataTable != null){
                    prStmtAdditionalMetadataTable.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /*
     * returns the submission of all users in the jsonObject
     * this method takes formPath,cutPoints and searchOptions as parameter
     */
    @Override
    public JSONObject getSubmissionsOfAllUsers(String formPath,
                                               String cutPoints, Map<String, String> searchOptions)
            throws FormsPortalException {
        Connection connection = null;
        JSONObject resultObj = new JSONObject();
        try {
            String orderby = JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED;
            String sort = JRGFinanceFormsPortalConstants.STR_DEFAULT_SORT;
            String limit = JRGFinanceFormsPortalConstants.STR_DEFAULT_LIMIT;
            String offset = JRGFinanceFormsPortalConstants.STR_DEFAULT_OFFSET;
            String searchValue = JRGFinanceFormsPortalConstants.STR_DEFAULT_SEARCH_VALUE;
            cutPoints = cutPoints != null ? cutPoints : JRGFinanceFormsPortalConstants.STR_DEFAULT_CUT_POINTS;
            connection = getConnection();
            Session currentSession = resourceResolverHelper.getResourceResolverAs(Session.class);
            if (StringUtils.isNotEmpty(formPath) && portalUtilsComponent.isReviewer(currentSession, formPath)) {
                if (searchOptions != null) {
                    orderby = searchOptions.get(JRGFinanceFormsPortalConstants.STR_ORDERBY) != null ? searchOptions.get(JRGFinanceFormsPortalConstants.STR_ORDERBY) : orderby;
                    sort = searchOptions.get(JRGFinanceFormsPortalConstants.STR_SORT) != null ? searchOptions.get(JRGFinanceFormsPortalConstants.STR_SORT) : sort;
                    limit = searchOptions.get(JRGFinanceFormsPortalConstants.STR_LIMIT) != null ? searchOptions.get(JRGFinanceFormsPortalConstants.STR_LIMIT) : limit;
                    offset = searchOptions.get(JRGFinanceFormsPortalConstants.STR_OFFSET) != null ? searchOptions.get(JRGFinanceFormsPortalConstants.STR_OFFSET) : offset;
                    searchValue = searchOptions.get(JRGFinanceFormsPortalConstants.STR_SEARCH) != null ? searchOptions.get(JRGFinanceFormsPortalConstants.STR_SEARCH) : searchValue;
                }
                List<String> cutPointsList = new ArrayList<String>(Arrays.asList(cutPoints.split(",")));
                List<String> metadataCoList = listAllColumns(connection);
                cutPointsList.retainAll(metadataCoList);
                String enclosedCutPoints = "`" + StringUtils.join(cutPointsList, "`,`") + "`";
                String sqlQueryStmt = "SELECT " + enclosedCutPoints + " from " + metadataTable + " where status = 'submitted'AND formPath= '" + formPath
                        + "' AND concat_ws(" + enclosedCutPoints + ") like '%" + searchValue + "%' ORDER BY '" + orderby + "' " + sort + " LIMIT " + limit
                        + " OFFSET " + offset;
                PreparedStatement sqlQPreparedStatement = connection.prepareStatement(sqlQueryStmt);
                ResultSet result = sqlQPreparedStatement.executeQuery();
                JSONArray respArr = new JSONArray();
                int resultCount = 0;
                while (result.next()) {
                    resultCount++;
                    JSONObject submissionObj = new JSONObject();
                    for (String column : cutPointsList) {
                        if (column.equals(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED)) {
                            Date d = new Date(Long.valueOf(result.getString(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED)));
                            submissionObj.put(column, d.toString());
                        } else {
                            submissionObj.put(column, result.getString(column));
                        }
                    }
                    respArr.put(submissionObj);
                }
                resultObj.put(JRGFinanceFormsPortalConstants.STR_TOTAL, resultCount);
                resultObj.put(JRGFinanceFormsPortalConstants.STR_ITEMS, respArr);
                return resultObj;
            }
        } catch (Exception e) {
            throw new FormsPortalException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new FormsPortalException(e);
                }
            }
        }
        return resultObj;
    }

    /**
     *This method adds the comment to the comment table related with a submission
     *@param submitID
     *@param commentContent
     *@param owner
     *@return the commentID of the newly added comment
     */
    @Override
    public String addComment(String submitID, String commentContent,
                             String owner) throws FormsPortalException {
        PreparedStatement prStmtCommentTable = null;
        try {
            Connection connection = getConnection();
            Session currentSession = resourceResolverHelper.getResourceResolverAs(Session.class);
            if (owner == null) {
                owner = currentSession.getUserID();
            }
            String formPath = getProperty(submitID, JRGFinanceFormsPortalConstants.STR_FORM_PATH)[0];
            if (StringUtils.isNotEmpty(formPath) && portalUtilsComponent.isReviewer(currentSession, formPath)) {
                String insertStmt = "INSERT INTO " + getCommentTableName() + " (ID, commentId, comment, commentowner, time) values ((?),(?),(?),(?),(?))";
                String commentID = fpKeyGeneratorService.getUniqueId();
                prStmtCommentTable = connection.prepareStatement(insertStmt);
                prStmtCommentTable.setString(1, submitID);
                prStmtCommentTable.setString(2, commentID);
                prStmtCommentTable.setString(3, commentContent);
                prStmtCommentTable.setString(4, owner);
                prStmtCommentTable.setString(5, String.valueOf(System.currentTimeMillis()));
                prStmtCommentTable.execute();
                return commentID;
            }
        } catch (Exception e) {
            throw new FormsPortalException(e);
        } finally {
            try {
                if (prStmtCommentTable != null) {
                    prStmtCommentTable.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e.getMessage(), e);
            }
        }
        return "";
    }

    /*
     * This method returns the distinct forms whose submission has been made and user is reviewer to those forms
     * @return:JSONArray containing resultant forms
     */
    @Override
    public JSONArray getFormsForSubmissionReview() throws FormsPortalException {
        Connection connection = null;
        ResultSet resultSet = null;
        Session currentSession = null;
        try {
            connection = getConnection();
            currentSession = resourceResolverHelper.getResourceResolverAs(Session.class);
            String getformPathStmt = "SELECT DISTINCT " + JRGFinanceFormsPortalConstants.STR_FORM_PATH + " FROM " + metadataTable + " WHERE status = 'submitted'";
            PreparedStatement formPathstmt = connection.prepareStatement(getformPathStmt);
            resultSet = formPathstmt.executeQuery();
            JSONArray respArr = new JSONArray();
            if (resultSet != null) {
                while (resultSet.next()) {
                    String formPath = null;
                    if (resultSet.getString(JRGFinanceFormsPortalConstants.STR_FORM_PATH) != null) {
                        formPath = resultSet.getString(JRGFinanceFormsPortalConstants.STR_FORM_PATH);
                    }
                    JSONObject currentForm = new JSONObject();
                    if (formPath != null && portalUtilsComponent.isReviewer(currentSession, formPath)) {
                        currentForm.put(JRGFinanceFormsPortalConstants.STR_FORM_PATH, formPath);
                        Node formNode = currentSession.getNode(formPath);
                        Node metadataNode = formNode.getNode(JRGFinanceFormsPortalConstants.STR_JCR_CONTENT + "/" + JRGFinanceFormsPortalConstants.STR_METADATA);
                        String formName = metadataNode != null ? metadataNode.getProperty(JRGFinanceFormsPortalConstants.STR_TITLE).getString() : formNode.getName();
                        currentForm.put(JRGFinanceFormsPortalConstants.STR_FORM_NAME, formName);
                        respArr.put(currentForm);
                    }
                }
            }
            return respArr;
        } catch (Exception e) {
            throw new FormsPortalException(e);
        } finally {
            try {
                if (currentSession.isLive()) {
                    currentSession.logout();
                }
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    /**
     * This method returns all the comments to the reviewer/owner of particular submission
     * @param submitID
     * @return a jsonArray containing comments along with its details
     */
    @Override
    public JSONArray getAllComments(String submitID) throws FormsPortalException {
        PreparedStatement prStmtCommentTable = null;
        Connection connection = null;
        JSONArray jsonArr = new JSONArray();
        try {
            connection = getConnection();
            Session currentSession = resourceResolverHelper.getResourceResolverAs(Session.class);
            String formPath = getProperty(submitID, JRGFinanceFormsPortalConstants.STR_FORM_PATH)[0];
            if (StringUtils.isNotEmpty(formPath) && portalUtilsComponent.isReviewer(currentSession, formPath)) {
                String queryStmt = "SELECT * from " + getCommentTableName() + " where ID = '" + submitID + "'";
                prStmtCommentTable = connection.prepareStatement(queryStmt);
                ResultSet rs = prStmtCommentTable.executeQuery();
                while (rs.next()) {
                    JSONObject commentObj = new JSONObject();
                    commentObj.put(JRGFinanceFormsPortalConstants.STR_COMMENT, rs.getString(JRGFinanceFormsPortalConstants.STR_COMMENT));
                    commentObj.put(JRGFinanceFormsPortalConstants.STR_OWNER, rs.getString(JRGFinanceFormsPortalConstants.STR_COMMENT_OWNER));
                    commentObj.put(JRGFinanceFormsPortalConstants.STR_TIME, rs.getString(JRGFinanceFormsPortalConstants.STR_TIME));
                    jsonArr.put(commentObj);
                }
                return jsonArr;
            }
        } catch (Exception e) {
            throw new FormsPortalException(e);
        } finally {
            try {
                if (prStmtCommentTable != null) {
                    prStmtCommentTable.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
        return jsonArr;
    }

    /* (non-Javadoc)
     * @see com.adobe.fd.fp.service.SubmitMetadataService#submitMetadataAsynchronously(java.util.Map)
     */
    @Override
    public JSONObject submitMetadataAsynchronously(Map<String, Object> submittedMetaPropMap)
            throws FormsPortalException {
        JSONObject resultJson = submitMetadata(submittedMetaPropMap);
        return resultJson;
    }

    private String flattentList(List<?> items) {
        List<String> itemsListVal = new ArrayList<String>();
        for(Object key : items){
            if (key != null) {
                itemsListVal.add(key.toString());
            }
        }
        return StringUtils.join(itemsListVal, "|");
    }

    private String flattenArray(Object[] items) {
        List<String> itemsListVal = new ArrayList<String>();
        for(Object key : items){
            if (key != null) {
                itemsListVal.add(key.toString());
            }

        }
        return StringUtils.join(itemsListVal, "|");
    }

    private String[] deflateList (String value) {
        String[] keys = new String[]{""};
        if (!StringUtils.isEmpty(value)) {
            List<?> items = new ArrayList<String>(Arrays.asList(value.split("\\|")));

            keys = new String[items.size()];
            keys = items.toArray(keys);
            return keys;
        }
        return keys;
    }

    @Override
    public JSONObject saveSignMetadata(Map<String, Object> metadataMap)
            throws FormsPortalException {
        Connection connection = null;
        PreparedStatement prStmt = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);

            String id = null;
            if(metadataMap.containsKey(PendingSignMetadata.PENDING_SIGN_ID)){
                id = metadataMap.get(PendingSignMetadata.PENDING_SIGN_ID).toString();
            } else {
                id = fpKeyGeneratorService.getUniqueId();
                metadataMap.put(PendingSignMetadata.PENDING_SIGN_ID, id);
            }
            metadataMap.put(JRGFinanceFormsPortalConstants.STR_ID, id);
            insertMetadata(id, metadataMap, connection);

            /**
             * Committing after all the operations
             */
            connection.commit();

            JSONObject submittedInstance = new JSONObject();
            String getSubmittedInstance = "SELECT * FROM " + getMetadataTableName() + " WHERE id = (?)";
            prStmt = connection.prepareStatement(getSubmittedInstance);
            prStmt.setString(1, id);
            ResultSet result = prStmt.executeQuery();
            if(result.next()){
                submittedInstance.put(PendingSignMetadata.PENDING_SIGN_ID, result.getString(PendingSignMetadata.PENDING_SIGN_ID));
                submittedInstance.put(JRGFinanceFormsPortalConstants.STR_FORM_NAME, result.getString(JRGFinanceFormsPortalConstants.STR_FORM_NAME));
                submittedInstance.put(JRGFinanceFormsPortalConstants.STR_OWNER, result.getString(JRGFinanceFormsPortalConstants.STR_OWNER));
                submittedInstance.put(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED, result.getString(JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED));
            }
            if(result != null){
                result.close();
            }
            return submittedInstance;
        } catch(Exception e){
            try{
                /**
                 *  In case of any error, rollback
                 */
                if(connection!=null){
                    connection.rollback();
                }
            }catch(SQLException e2){
                throw new FormsPortalException(e2.getMessage(), e2);
            }
            throw new FormsPortalException(e);
        } finally{
            try {
                /**
                 * Close the statement and connection in finally block
                 */
                if(prStmt != null){
                    prStmt.close();
                }
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    @Override
    public JSONArray getPendingSignInstances(String cutPoints)
            throws FormsPortalException {
        Connection connection = null;
        try{
            connection = getConnection();
            return listItems(JRGFinanceFormsPortalConstants.STR_FP_PENDING_SIGN_FORM, cutPoints, connection);
        } catch(Exception e){
            throw new FormsPortalException(e);
        } finally{
            try {
                /**
                 * Close the connection in finally block
                 */
                if(connection != null){
                    connection.close();
                }
            } catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    @Override
    public JSONObject readPendingSignInstance(String pendingSignID,
                                              String cutPoints) throws FormsPortalException {
        PreparedStatement prStmt = null;
        ResultSet resultSet = null;
        Connection connection = null;
        JSONObject resultObj = null;
        try{
            connection = getConnection();
            String getItemsStmt = "SELECT * FROM " + getMetadataTableName() + " LEFT JOIN " + getAdditionalMetadataTableName() +
                    " ON " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_ID + " = " + getAdditionalMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_ID +
                    " WHERE " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_NODE_TYPE + " = (?) "+
                    "AND " + getMetadataTableName() + "." + PendingSignMetadata.PENDING_SIGN_ID + " = (?)" +
                    "AND " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_USERDATA_ID + " IS NOT NULL "+
                    " order by `" + JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED + "` asc";

            prStmt = connection.prepareStatement(getItemsStmt);
            prStmt.setString(1, JRGFinanceFormsPortalConstants.STR_FP_PENDING_SIGN_FORM);
            prStmt.setString(2, pendingSignID);
            JSONArray readItems = readItems(prStmt, JRGFinanceFormsPortalConstants.STR_FP_PENDING_SIGN_FORM, cutPoints, connection);
            if (readItems.length() > 0) {
                resultObj = readItems.getJSONObject(0);
            }
        } catch(Exception e){
            throw new FormsPortalException(e);
        } finally{
            try {
                if(resultSet != null){
                    resultSet.close();
                }
                if(prStmt != null){
                    prStmt.close();
                }
                /**
                 * Close the connection in finally block
                 */
                if(connection != null){
                    connection.close();
                }
            }catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
        return resultObj;
    }

    @Override
    public JSONArray searchPendingSignInstances(Query query)
            throws FormsPortalException {
        PreparedStatement prStmt = null;
        ResultSet resultSet = null;
        Connection connection = null;
        try{
            connection = getConnection();
            String getItemsStmt = "SELECT * FROM " + getMetadataTableName() + " LEFT JOIN " + getAdditionalMetadataTableName() +
                    " ON " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_ID + " = " + getAdditionalMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_ID +
                    " WHERE " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_NODE_TYPE + " = (?) "+
                    "AND " + getMetadataTableName() + "." + JRGFinanceFormsPortalConstants.STR_USERDATA_ID + " IS NOT NULL ";

            getItemsStmt+=transformStatementGroup(query.getStatementGroup());
            getItemsStmt+=" order by `" + JRGFinanceFormsPortalConstants.STR_JCR_LAST_MODIFIED + "` asc";

            prStmt = connection.prepareStatement(getItemsStmt);
            prStmt.setString(1, JRGFinanceFormsPortalConstants.STR_FP_PENDING_SIGN_FORM);
            return readItems(prStmt, JRGFinanceFormsPortalConstants.STR_FP_PENDING_SIGN_FORM, query.getCutPoints(), connection);
        } catch(Exception e){
            throw new FormsPortalException(e);
        } finally{
            try {
                if(resultSet != null){
                    resultSet.close();
                }
                if(prStmt != null){
                    prStmt.close();
                }
                /**
                 * Close the connection in finally block
                 */
                if(connection != null){
                    connection.close();
                }
            }catch (SQLException e) {
                throw new FormsPortalException(e);
            }
        }
    }

    private String transformStatement(Statement statement) {
        String constraint = "";
        if (statement.getAttributeName() != null && statement.getAttributeValue() != null) {
            constraint = " " + getMetadataTableName() + "." + statement.getAttributeName() + sqlOperatorMap.get(statement.getOperator());
            if (statement.getOperator() != Operator.EXISTS) {
                constraint+= statement.getAttributeValue() ;
            }
            constraint += " ";
        }
        return constraint;
    }

    private String transformStatementGroup(StatementGroup statementGroup) {
        String constraint = "";
        if (statementGroup != null && statementGroup.getStatements() != null) {
            for (Statement statement: statementGroup.getStatements()) {
                constraint+=transformStatement(statement);
            }
        }
        return constraint;
    }

    private String getCommentTableName() {
        return commentTable;
    }
}
