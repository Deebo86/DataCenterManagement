package com.example.Task1.controller;


import com.example.Task1.dto.*;
import com.example.Task1.service.IServerService;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/servers")
@Tag(name = "ServerOperations", description = "All operations related to servers")
@Validated
public class ServerController {
    @Autowired
    private IServerService service;

    @Operation(summary = "Get server by id", tags = {"ServerOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the server",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServerResponseDto.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Server not found",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @GetMapping("/{serverId}")
    @Validated //checks for validation constraints on the path variable
    public ResponseEntity<ServerResponseDto> getServer(@PathVariable @Min(1) Integer serverId)
    {
        return new ResponseEntity<>(service.getById(serverId), HttpStatus.OK);
    }

    @Operation(summary = "Get all servers", tags = {"ServerOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found servers",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ServerResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }) })
    @GetMapping
    public ResponseEntity<List<ServerResponseDto>> getAllServers()
    {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @Operation(summary = "Add a new server", tags = {"ServerOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server added",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid fields supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error - Insertion Failure",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) })})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = ServerRequestDto.class)))

    @PostMapping()
    public ResponseEntity<String> addServer(@org.springframework.web.bind.annotation.RequestBody @Valid ServerRequestDto s) throws Exception
    {
        service.create(s);
        return new ResponseEntity<>("Created", HttpStatus.CREATED);
    }

    @Operation(summary = "Update a specific server", tags = {"ServerOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server updated",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied or Invalid fields supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
//            @ApiResponse(responseCode = "400", description = "Invalid fields supplied",
//                    content = { @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Server not found",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error - Update Failure",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) })})
    @RequestBody(content = @Content(
            schema = @Schema(implementation = ServerUpdateRequestDto.class)))
    @PutMapping("/{serverId}")
    public ResponseEntity<String> updateServer (@org.springframework.web.bind.annotation.RequestBody @Valid ServerUpdateRequestDto s, @PathVariable @Min(1) Integer serverId)
    {
        service.update(s, serverId);
        return new ResponseEntity<>("Updated", HttpStatus.ACCEPTED);
    }


    @Operation(summary = "Delete a specific server", tags = {"ServerOperations"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted the server",
                    content = @Content(mediaType = "application/json") ),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Server not found",
                    content =  { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Server Error - Deletion Failure",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)) })})
    @DeleteMapping("/{serverId}")
    public ResponseEntity<String> deleteServer(@PathVariable @Min(1) @Valid Integer serverId)
    {
        service.delete(serverId);
        return new ResponseEntity<>("Deleted", HttpStatus.ACCEPTED);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> addServersFromFile(MultipartFile file)
    {
        service.create(file);
        return new ResponseEntity<>("Created", HttpStatus.CREATED);
    }

    @GetMapping("/downloadAll")
    public ResponseEntity<InputStreamResource> downloadServers()
    {
        ByteArrayInputStream in = service.download();

        return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=serverData.xlsx").contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(new InputStreamResource(in));
    }
}
