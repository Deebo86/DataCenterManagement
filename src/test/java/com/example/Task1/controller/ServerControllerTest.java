package com.example.Task1.controller;

import com.example.Task1.dto.*;
import com.example.Task1.exception.ServerNotFoundException;
import com.example.Task1.model.enums.OperatingSystem;
import com.example.Task1.service.IServerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ServerController.class)
class ServerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IServerService serviceMock;

    private ServerResponseDto responseDto;

    private List<ServerResponseDto> responseList;

    @BeforeEach
    void setup() {
        responseDto = new ServerResponseDto();
        responseDto.setId(1);
        responseDto.setDatacenterId(-1);
        responseDto.setMemory(40);
        responseDto.setNoOfCPU(5);
        responseDto.setOs(OperatingSystem.WINDOWS);

        responseList = Arrays.asList(responseDto,
                new ServerResponseDto(2, 1, 20, 3, OperatingSystem.LINUX));
    }

    @Nested
    class GetServer {
        @Test
        void should_ReturnServer_When_GivenValidId() throws Exception {
            when(serviceMock.getById(anyInt())).thenReturn(responseDto);

            mockMvc.perform(get("/api/servers/{id}", responseDto.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(responseDto.getId()))
                    .andExpect(jsonPath("$.datacenterId").value(responseDto.getDatacenterId()))
                    .andExpect(jsonPath("$.memory").value(responseDto.getMemory()))
                    .andExpect(jsonPath("$.noOfCPU").value(responseDto.getNoOfCPU()))
                    .andExpect(jsonPath("$.os").value(responseDto.getOs().name()));
            verify(serviceMock, times(1)).getById(anyInt());

        }

        @Test
        void should_ReturnInvalidId_When_GivenInvalidIdValueString() throws Exception {
            mockMvc.perform(get("/api/servers/{id}", "x"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName").value("MethodArgumentTypeMismatchException"));

            verifyNoInteractions(serviceMock);
        }

        @Test
        void should_ReturnServerNotFound_When_ServerNotExist() throws Exception {
            int id = 1;
            when(serviceMock.getById(anyInt())).thenThrow(ServerNotFoundException.class);
            mockMvc.perform(get("/api/servers/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorName").value("ServerNotFoundException"));

            verify(serviceMock, times(1)).getById(anyInt());
            verifyNoMoreInteractions(serviceMock);

        }
    }

    @Nested
    class GetAllServers {
        @Test
        void should_ReturnListOfServers_When_ServersExist_HappyPath() throws Exception {
            when(serviceMock.getAll()).thenReturn(responseList);

            mockMvc.perform(get("/api/servers"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(responseList.size()));

            verify(serviceMock, times(1)).getAll();
        }

        @Test
        void should_ReturnEmptyListOfServers_When_NoServers() throws Exception {
            when(serviceMock.getAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/servers"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(serviceMock, times(1)).getAll();
        }

        @Test
        void should_ReturnServerError_When_ProblemFetching() throws Exception {
            when(serviceMock.getAll()).thenThrow(RuntimeException.class);

            mockMvc.perform(get("/api/servers"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            verify(serviceMock, times(1)).getAll();
        }

    }

    @Nested
    class AddServer {
        private ServerRequestDto requestDto;

        @BeforeEach
        void setup() {
            requestDto = new ServerRequestDto(40, 3, OperatingSystem.WINDOWS);
        }

        @Test
        void should_CreateServer_When_AllRequiredDataPresent() throws Exception {
            doNothing().when(serviceMock).create(requestDto);

            mockMvc.perform(post("/api/servers")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated());

            verify(serviceMock, times(1)).create(requestDto);
        }

        @Test
        void should_Return4xx_When_NoOperatingSystem() throws Exception {
            requestDto = new ServerRequestDto();
            requestDto.setMemory(40);
            requestDto.setNoOfCPU(3);

            mockMvc.perform(post("/api/servers")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.reasons").isArray())
                    .andExpect(jsonPath("$.reasons[0]", containsString("Validation failed for field 'os'")))
                    .andExpect(jsonPath("$.reasons.size()").value(1));

            verifyNoInteractions(serviceMock);
        }

        @ParameterizedTest(name = "Test with memory = {0}")
        @CsvSource(value = {"0", "7"})
        void should_Return4xx_When_InvalidMemory(int memory) throws Exception {
            requestDto = new ServerRequestDto();
            requestDto.setMemory(memory);
            requestDto.setNoOfCPU(3);
            requestDto.setOs(OperatingSystem.WINDOWS);

            mockMvc.perform(post("/api/servers")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.reasons").isArray())
                    .andExpect(jsonPath("$.reasons[0]", containsString("Validation failed for field 'memory'")))
                    .andExpect(jsonPath("$.reasons.size()").value(1));

            verifyNoInteractions(serviceMock);
        }

        @ParameterizedTest(name = "Test with NoOfCpu = {0}")
        @CsvSource({"0", "16"})
        void should_Return4xx_When_InvalidCpu(int cpu) throws Exception {
            requestDto = new ServerRequestDto();
            requestDto.setMemory(50);
            requestDto.setNoOfCPU(cpu);
            requestDto.setOs(OperatingSystem.WINDOWS);

            mockMvc.perform(post("/api/servers")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.reasons").isArray())
                    .andExpect(jsonPath("$.reasons[0]", containsString("Validation failed for field 'noOfCPU'")))
                    .andExpect(jsonPath("$.reasons.size()").value(1));

            verifyNoInteractions(serviceMock);
        }

        @Test
        void should_Return4xxWithArrayOf3_When_AllMissingFields() throws Exception {
            requestDto = new ServerRequestDto();

            mockMvc.perform(post("/api/servers")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.reasons").isArray())
                    .andExpect(jsonPath("$.reasons.size()").value(3));

            verifyNoInteractions(serviceMock);
        }
    }

    @Nested
    class UpdateServer {
        private ServerUpdateRequestDto requestDto;

        @BeforeEach
        void setup() {
            requestDto = new ServerUpdateRequestDto(40, 3, OperatingSystem.WINDOWS);
        }

        @Test
        void should_Update_When_AllValidAllDefined_HappyPath() throws Exception {
            int id = 1;

            mockMvc.perform(put("/api/servers/{serverId}", id).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isAccepted());

            verify(serviceMock, times(1)).update(requestDto, id);
        }

        static Stream<Arguments> updateDtoTestCases() {
            return Stream.of(
                    Arguments.of(new ServerUpdateRequestDto(null, null, OperatingSystem.WINDOWS)),
                    Arguments.of(new ServerUpdateRequestDto(40, null, null)),
                    Arguments.of(new ServerUpdateRequestDto(null, 5, null)));
        }

        @ParameterizedTest
        @MethodSource("updateDtoTestCases")
        void should_Update_When_OneFieldSpecified_HappyPath(ServerUpdateRequestDto dto) throws Exception {
            int id = 1;

            mockMvc.perform(put("/api/servers/{serverId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isAccepted());
            verify(serviceMock, times(1)).update(dto, id);
        }

        static Stream<Arguments> memoryUpdateTestCases()
        {
            return Stream.of(
                    Arguments.of(new ServerUpdateRequestDto(5, null, null)),
                    Arguments.of(new ServerUpdateRequestDto(4, null, OperatingSystem.WINDOWS)));
        }
        @ParameterizedTest
        @MethodSource("memoryUpdateTestCases")
        void should_Return400_When_MemoryInvalid(ServerUpdateRequestDto dto) throws Exception {
            int id = 1;

            mockMvc.perform(put("/api/servers/{serverId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.reasons").isArray()).andExpect(jsonPath("$.reasons.length()").value(1))
                    .andExpect(jsonPath("$.errorName").value("MethodArgumentNotValidException"));

            verify(serviceMock, never()).update(dto, id);
        }

        @ParameterizedTest
        @CsvSource(value = {"11","80"})
        void should_Return400_When_NoOfCPUInvalid(Integer cpu) throws Exception {
            int id = 1;
            requestDto = new ServerUpdateRequestDto();
            requestDto.setNoOfCPU(cpu);


            mockMvc.perform(put("/api/servers/{serverId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.reasons").isArray()).andExpect(jsonPath("$.reasons.length()").value(1))
                    .andExpect(jsonPath("$.errorName").value("MethodArgumentNotValidException"));

            verify(serviceMock, never()).update(requestDto, id);
        }

        @Test
        void should_Return404_When_ServerNotFound() throws Exception {
            int id = 1;
            doThrow(ServerNotFoundException.class).when(serviceMock).update(requestDto, id);

            mockMvc.perform(put("/api/servers/{serverId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorName").value("ServerNotFoundException"));
        }

        static Stream<Arguments> invalidPathVarTestCases()
        {
            return Stream.of(
                    Arguments.of("adf", "MethodArgumentTypeMismatchException"),
                    Arguments.of(-1, "ConstraintViolationException"));
        }

        @ParameterizedTest
        @MethodSource("invalidPathVarTestCases")
        void should_Return400_When_InvalidId(Object param, String exception) throws Exception
        {

            mockMvc.perform(put("/api/servers/{serverId}", param)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName").value(exception));

            verifyNoInteractions(serviceMock);
        }
    }

    @Nested
    class DeleteServer {
        @Test
        void should_ReturnAccepted_When_HappyPath() throws Exception{
            int id = 1;

            mockMvc.perform(delete("/api/servers/{serverId}", id))
                    .andExpect(status().isAccepted());

            verify(serviceMock, times(1)).delete(id);
        }

        static Stream<Arguments> invalidPathVarTestCases()
        {
            return Stream.of(
                    Arguments.of("adf", "MethodArgumentTypeMismatchException"),
                    Arguments.of(-1, "ConstraintViolationException"));
        }

        @ParameterizedTest
        @MethodSource("invalidPathVarTestCases")
        void should_Return400_When_InvalidId(Object param, String exception) throws Exception
        {

            mockMvc.perform(delete("/api/servers/{serverId}", param))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName").value(exception));

            verifyNoInteractions(serviceMock);
        }

        @Test
        void should_Return404_When_ServerNotFound() throws Exception {
            int id = 1;
            doThrow(ServerNotFoundException.class).when(serviceMock).delete(id);

            mockMvc.perform(delete("/api/servers/{serverId}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorName").value("ServerNotFoundException"));
        }
    }

    //TODO: tests for the upload and download
}