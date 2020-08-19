package org.alliancegenome.api.rest.interfaces;

import com.fasterxml.jackson.annotation.JsonView;
import org.alliancegenome.cache.repository.helper.JsonResultResponse;
import org.alliancegenome.core.StatisticRow;
import org.alliancegenome.core.TransgenicAlleleStats;
import org.alliancegenome.neo4j.entity.node.DOTerm;
import org.alliancegenome.neo4j.view.View;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/statistics")
@Tag(name = "Statistics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface StatisticsRESTInterface {


    @GET
    @Path("/transgenic-alleles")
    @JsonView(value = {View.DiseaseAPI.class})
    @Operation(summary = "Retrieve transgenic allele stats")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "404",
                            description = "Missing transgenic allele object",
                            content = @Content(mediaType = "text/plain")),
                    @APIResponse(
                            responseCode = "200",
                            description = "transgenic allele object.",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DOTerm.class)))})
    JsonResultResponse<StatisticRow> getTrans(
            @Parameter(in = ParameterIn.QUERY, name = "filter.geneSpecies", description = "genetic entity symbol", schema = @Schema(type = SchemaType.STRING))
            @QueryParam("filter.geneSpecies") String geneSpecies,
            @Parameter(in = ParameterIn.QUERY, name = "filter.subEntity", description = "genetic entity symbol", schema = @Schema(type = SchemaType.STRING))
            @QueryParam("filter.subEntity") String filterSubEntity,
            @Parameter(in = ParameterIn.QUERY, name = "sortBy", description = "sort by a given field", schema = @Schema(type = SchemaType.STRING))
            @QueryParam("sortBy") String sortBy

            );

}
