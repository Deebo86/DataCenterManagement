package com.example.Task1.controller;

import com.example.Task1.dto.*;
import com.example.Task1.service.IDataCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DataCenterOperations", description = "All operations related to datacenters")
@RestController
@RequestMapping("/api/datacenters")
@Validated
public class DataCenterController {
    @Autowired
    private IDataCenterService service;

    @Operation(summary = "Get details of a specific datacenter", tags = {"DataCenterOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the datacenter",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DataCenterResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Datacenter not found",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @GetMapping("/{dataCenterId}")
    public ResponseEntity<DataCenterResponseDto> getDataCenterById(@PathVariable @Min(1) @Valid Integer dataCenterId){
        return new ResponseEntity<>(service.getById(dataCenterId), HttpStatus.OK);
    }

    @Operation(summary = "Get country, maximum server capacity, and current server capacity of all datacenter", tags = {"DataCenterOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the datacenters",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DataCenterSummarizedDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @GetMapping("/summarized")
    public ResponseEntity<List<DataCenterSummarizedDto>> getAllSummarized(){
        return new ResponseEntity<>(service.getAllSummarized(), HttpStatus.OK);
    }

    @Operation(summary = "Get details of all datacenter", tags = {"DataCenterOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the datacenters",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DataCenterResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @GetMapping()
    public ResponseEntity<List<DataCenterResponseDto>> getAllDataCenters()
    {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Add a datacenter", tags = {"DataCenterOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the datacenter",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DataCenterResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid fields supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @RequestBody(content = @Content(
            schema = @Schema(implementation = DataCenterRequestDto.class)))
    @PostMapping()
    public ResponseEntity<String> addDataCenter(@org.springframework.web.bind.annotation.RequestBody @Valid DataCenterRequestDto dc)
    {
        service.create(dc);
        return new ResponseEntity<>("Created", HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a specific datacenter", tags = {"DataCenterOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted the datacenter",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Datacenter not found",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
            @ApiResponse(responseCode = "500", description = "Server Error",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) })
    @DeleteMapping("/{dataCenterId}")
    @Validated
    public ResponseEntity<String> deleteDataCenter(@PathVariable @Min(1) int dataCenterId)
    {
        service.delete(dataCenterId);
        return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }

    @Operation(summary = "Update a specific datacenter", tags = {"DataCenterOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated the datacenter",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied or Invalid fields supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
//            @ApiResponse(responseCode = "400", description = "Invalid fields supplied",
//                    content = { @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "DataCenter not Found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @RequestBody(content = @Content(
            schema = @Schema(implementation = DataCenterRequestDto.class)))
    @PutMapping("/{dataCenterId}")
    @Validated
    public ResponseEntity<String> updateDataCenter(@org.springframework.web.bind.annotation.RequestBody @Valid DataCenterUpdateRequestDto dc, @Valid @PathVariable @Min(1) Integer dataCenterId)
    {
        service.update(dc, dataCenterId);
        return new ResponseEntity<>("Updated", HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Assign servers to a datacenter or update a datacenter's servers", tags = {"DataCenterOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Added/Updated the datacenter's servers",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid datacenter id supplied or Invalid serverIds supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
//            @ApiResponse(responseCode = "400", description = "Invalid serverIds supplied",
//                    content = { @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Datacenter not Found or Servers not Found",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "409", description = "Datacenter server capacity surpassed",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @RequestBody(content = @Content(
            schema = @Schema(implementation = DataCenterServersAddOrUpdateRequestDto.class)))
    @PatchMapping("/{dataCenterId}/servers")
    @Validated
    public ResponseEntity<String> addOrUpdateServers(@org.springframework.web.bind.annotation.RequestBody @Valid DataCenterServersAddOrUpdateRequestDto servers, @PathVariable @Valid @Min(1) Integer dataCenterId)
    {
        service.addOrUpdateServers(servers, dataCenterId);
        return new ResponseEntity<>("Updated", HttpStatus.ACCEPTED);
    }
}
