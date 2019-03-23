package org.alliancegenome.api.rest.interfaces;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.alliancegenome.core.service.JsonResultResponse;
import org.alliancegenome.neo4j.entity.node.OrthoAlgorithm;
import org.alliancegenome.neo4j.view.OrthologView;
import org.alliancegenome.neo4j.view.View;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/homologs")
@Api(value = "Homology")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface OrthologyRESTInterface {

    @GET
    @Path("/{taxonIDOne}/{taxonIDTwo}")
    @JsonView(value={View.Orthology.class})
    @ApiOperation(value = "Retrieve homologous gene records for given pair of species")
    JsonResultResponse<OrthologView> getDoubleSpeciesOrthology(
            @ApiParam(name = "taxonIDOne", value = "Taxon ID for the first gene: Could be the full ID, e.g. 'NCBITaxon:10090', or just the ID, i.e. '10090'. Alternatively, part of a species name uniquely identifying a single species, e.g. 'danio' or 'mus'.", required = true, type = "String")
            @PathParam("taxonIDOne") String speciesOne,
            @ApiParam(name = "taxonIDTwo", value = "Taxon ID for the second gene: Could be the full ID, e.g. 'NCBITaxon:10090', or just the ID, i.e. '10090'. Alternatively, part of a species name uniquely identifying a single species, e.g. 'danio' or 'mus'.", required = true, type = "String")
            @PathParam("taxonIDTwo") String speciesTwo,
            @ApiParam(value = "Select a stringency containsFilterValue", allowableValues = "stringent, moderate, all", defaultValue = "stringent")
            @QueryParam("stringencyFilter") String stringencyFilter,
            @ApiParam(value = "Select a calculation method", allowableValues = "Ensembl Compara, HGNC, Hieranoid, InParanoid, OMA, OrthoFinder, OrthoInspector, PANTHER, PhylomeDB, Roundup, TreeFam, ZFIN")
            @QueryParam("methods") String methods,
            @ApiParam(value = "Number of returned rows")
            @DefaultValue("20") @QueryParam("rows") Integer rows,
            @ApiParam(value = "Starting row")
            @DefaultValue("1") @QueryParam("start") Integer start) throws IOException;

    @GET
    @Path("/{taxonID}")
    @JsonView(value={View.Orthology.class})
    @ApiOperation(value = "Retrieve homologous gene records for a given species")
    JsonResultResponse<OrthologView> getSingleSpeciesOrthology(
            @ApiParam(name = "taxonID", value = "Taxon ID for the gene: Could be the full ID, e.g. 'NCBITaxon:10090', or just the ID, i.e. '10090'. Alternatively, part of a species name uniquely identifying a single species, e.g. 'danio' or 'mus'.", required = true, type = "String")
            @PathParam("taxonID") String species,
            @ApiParam(value = "Select a stringency containsFilterValue", allowableValues = "stringent, moderate, all", defaultValue = "stringent")
            @QueryParam("stringencyFilter") String stringencyFilter,
            @ApiParam(value = "Select a calculation method", allowableValues = "Ensembl Compara, HGNC, Hieranoid, InParanoid, OMA, OrthoFinder, OrthoInspector, PANTHER, PhylomeDB, Roundup, TreeFam, ZFIN")
            @QueryParam("methods") String methods,
            @ApiParam(value = "Number of returned rows")
            @DefaultValue("20") @QueryParam("rows") Integer rows,
            @ApiParam(value = "Starting row")
            @DefaultValue("1") @QueryParam("start") Integer start) throws IOException;

    @GET
    @Path("/species")
    @JsonView(value={View.Orthology.class})
    @ApiOperation(value = "Retrieve homologous gene records for given list of species")
    JsonResultResponse<OrthologView> getMultiSpeciesOrthology(
            @ApiParam(name = "taxonID", value = "List of taxon IDs for which homology is retrieved, e.g. 'NCBITaxon:10090'")
            @QueryParam("taxonID") List<String> taxonID,
            @ApiParam(name = "taxonIdList", value = "List of source taxon IDs for which homology is retrieved in a comma-delimited list, e.g. 'MGI:109583,RGD:2129,MGI:97570")
            @QueryParam("taxonIdList") String taxonIdList,
            @ApiParam(value = "Select a stringency containsFilterValue", allowableValues = "stringent, moderate, all", defaultValue = "stringent")
            @QueryParam("stringencyFilter") String stringencyFilter,
            @ApiParam(value = "Select a calculation method", allowableValues = "Ensembl Compara, HGNC, Hieranoid, InParanoid, OMA, OrthoFinder, OrthoInspector, PANTHER, PhylomeDB, Roundup, TreeFam, ZFIN")
            @QueryParam("methods") String methods,
            @ApiParam(value = "Number of returned rows")
            @DefaultValue("20") @QueryParam("rows") Integer rows,
            @ApiParam(value = "Starting row")
            @DefaultValue("1") @QueryParam("start") Integer start) throws IOException;

    @GET
    @Path("/geneMap")
    @JsonView(value={View.Orthology.class})
    @ApiOperation(value = "Retrieve homologous gene records for given list of geneMap")
    JsonResultResponse<OrthologView> getMultiGeneOrthology(
            @ApiParam(name = "geneID", value = "List of geneMap (specified by their ID) for which homology is retrieved, e.g. 'MGI:109583'")
            @QueryParam("geneID") List<String> geneID,
            @ApiParam(name = "geneIdList", value = "List of additional source gene IDs for which homology is retrieved in a comma-delimited list, e.g. 'MGI:109583,RGD:2129,MGI:97570")
            @QueryParam("geneIdList") String geneList,
            @ApiParam(value = "Select a stringency containsFilterValue", allowableValues = "stringent, moderate, all", defaultValue = "stringent")
            @QueryParam("stringencyFilter") String stringencyFilter,
            @ApiParam(value = "calculation methods", allowableValues = "Ensembl Compara, HGNC, Hieranoid, InParanoid, OMA, OrthoFinder, OrthoInspector, PANTHER, PhylomeDB, Roundup, TreeFam, ZFIN")
            @QueryParam("methods") List<String> methods,
            @ApiParam(value = "Number of returned rows")
            @DefaultValue("20") @QueryParam("rows") Integer rows,
            @ApiParam(value = "Starting row")
            @DefaultValue("1") @QueryParam("start") Integer start) throws IOException;

    @GET
    @Path("/methods")
    @JsonView(value={View.OrthologyMethod.class})
    @ApiOperation(value = "Retrieve all methods used for calculation of homology")
    JsonResultResponse<OrthoAlgorithm> getAllMethodsCalculations() throws JsonProcessingException;
}
