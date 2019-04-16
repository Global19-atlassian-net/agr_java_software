package org.alliancegenome.agr_submission.interfaces;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.alliancegenome.agr_submission.entities.DataType;
import org.alliancegenome.agr_submission.forms.CreateSchemaFileForm;
import org.alliancegenome.agr_submission.views.View;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("datatype")
@Api(value = "Data Type Endpoints")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DataTypeControllerInterface {

    @POST
    @Path("/")
    @ApiOperation("Create Entity")
    @JsonView(View.DataTypeCreate.class)
    public DataType create(@ApiParam(value = "Create: Entity") DataType entity);
    
    @GET
    @Path("/{id}")
    @ApiOperation("Reads Entity")
    @JsonView(View.DataTypeRead.class)
    public DataType get(@ApiParam(value = "Read: id") @PathParam("id") Long id);
    
    @PUT
    @Path("/")
    @ApiOperation("Update Entity")
    @JsonView(View.DataTypeUpdate.class)
    public DataType update(@ApiParam(value = "Update: Entity") DataType entity);
    
    @DELETE
    @Path("/{id}")
    @ApiOperation("Delete Entity")
    @JsonView(View.DataTypeDelete.class)
    public DataType delete(@ApiParam(value = "Delete: Entity") @PathParam("id") Long id);
    
    @GET
    @Path("/all")
    @ApiOperation("Get All Entities")
    @JsonView(View.DataTypeView.class)
    public List<DataType> getDataTypes();
    
    @POST
    @Path("/{dataType}/addschemafile")
    @ApiOperation("Adds a Schema File to this datatype")
    @JsonView({View.DataTypeView.class})
    public DataType addSchemaFile(
            @ApiParam(value = "DataType: id") @PathParam("dataType") String dataType,
            @ApiParam(value = "Form: Data") CreateSchemaFileForm form
    );

}
