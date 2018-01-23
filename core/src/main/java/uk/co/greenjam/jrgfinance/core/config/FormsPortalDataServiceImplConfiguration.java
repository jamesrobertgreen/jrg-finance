package uk.co.greenjam.jrgfinance.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import uk.co.greenjam.jrgfinance.core.utils.JRGFinanceFormsPortalConstants;

@ObjectClassDefinition(name = "Forms Portal Data Service Implementation Configuration")
public @interface FormsPortalDataServiceImplConfiguration {
    @AttributeDefinition(
            name = "datatable",
            description = "Name of the table to store data blob"
    )
    String getDataTable() default JRGFinanceFormsPortalConstants.STR_DEFAULT_DATA_TABLE;

    @AttributeDefinition(
            name = "datasource",
            description = "Name of the configured Data Source"
    )
    String getDataSource() default JRGFinanceFormsPortalConstants.STR_DEFAULT_DATA_SOURCE_NAME;

}
