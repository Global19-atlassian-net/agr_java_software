
package org.alliancegenome.agr_submission.views;

public class View {
    
    public class Default { }
    public class API extends Default { }

    public class DataTypeView extends API { }
    public class DataTypeCreate extends DataTypeView { }
    public class DataTypeRead extends DataTypeView { }
    public class DataTypeUpdate extends DataTypeView { }
    public class DataTypeDelete extends DataTypeView { }
    
    public class DataSubTypeView extends API { }
    public class DataSubTypeCreate extends DataSubTypeView { }
    public class DataSubTypeRead extends DataSubTypeView { }
    public class DataSubTypeUpdate extends DataSubTypeView { }
    public class DataSubTypeDelete extends DataSubTypeView { }
    
    public class ReleaseVersionView extends API { }
    public class ReleaseVersionCreate extends ReleaseVersionView { }
    public class ReleaseVersionRead extends ReleaseVersionView { }
    public class ReleaseVersionUpdate extends ReleaseVersionView { }
    public class ReleaseVersionDelete extends ReleaseVersionView { }
    
    public class SchemaVersionView extends API { }
    public class SchemaVersionCreate extends SchemaVersionView { }
    public class SchemaVersionRead extends SchemaVersionView { }
    public class SchemaVersionUpdate extends SchemaVersionView { }
    public class SchemaVersionDelete extends SchemaVersionView { }
}
