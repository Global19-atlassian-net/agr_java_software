package org.alliancegenome.api.rest.interfaces;

import com.fasterxml.jackson.annotation.JsonView;
import org.alliancegenome.core.TransgenicAlleleStats;
import org.alliancegenome.neo4j.entity.node.DOTerm;
import org.alliancegenome.neo4j.view.View;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    TransgenicAlleleStats getTrans();

}
