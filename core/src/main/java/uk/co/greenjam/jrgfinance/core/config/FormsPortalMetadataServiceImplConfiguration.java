package uk.co.greenjam.jrgfinance.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import uk.co.greenjam.jrgfinance.core.utils.JRGFinanceFormsPortalConstants;

@ObjectClassDefinition(name = "Forms Portal Metadata Service Implementation Configuration")
public @interface FormsPortalMetadataServiceImplConfiguration {

    @AttributeDefinition(
            name = "datasource",
            description = "Name of the configured Data Source"
    )
    String getDataSource() default JRGFinanceFormsPortalConstants.STR_DEFAULT_DATA_SOURCE_NAME;

    @AttributeDefinition(
            name = "metadatatable",
            description = "Name of the table to store out of the box metadata"
    )
    String getMetadataTable() default JRGFinanceFormsPortalConstants.STR_DEFAULT_METADATA_TABLE;

    @AttributeDefinition(
            name = "additionalmetadatatable",
            description = "Name of the table to store additional metadata"
    )
    String getAdditionalMetadataTable() default JRGFinanceFormsPortalConstants.STR_DEFAULT_ADDITIONAL_METADATA_TABLE;

    @AttributeDefinition(
            name = "commenttable",
            description = "Name of the table to store comments of reviewers on form submissions"
    )
    String getCommentTable() default JRGFinanceFormsPortalConstants.STR_COMMENT_TABLE;
}

