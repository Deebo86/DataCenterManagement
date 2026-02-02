package com.example.Task1.controller;

import com.example.Task1.dto.*;
import com.example.Task1.exception.*;
import com.example.Task1.model.enums.DataCenterType;
import com.example.Task1.service.IDataCenterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataCenterController.class)
class DataCenterControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IDataCenterService serviceMock;

    private DataCenterRequestDto request;

    private DataCenterResponseDto responseDto;

    private List<DataCenterResponseDto> responses;

    @BeforeEach
    void setUp() {
        responseDto = new DataCenterResponseDto();
        responseDto.setId(1);
        responseDto.setCountry("Egypt");
        responseDto.setAddress("Test address");
        responseDto.setDataCenterType(DataCenterType.CLOUD);

        DataCenterResponseDto m1 = new DataCenterResponseDto();
        m1.setId(2);
        m1.setCountry("USA");
        m1.setAddress("Test address");
        m1.setDataCenterType(DataCenterType.AI);

        responses = new ArrayList<>(Arrays.asList(responseDto, m1));
    }

    @Nested
    class GetById {
        @Test
        void should_Return200_When_HappyPath() throws Exception {
            int id = 1;

            when(serviceMock.getById(id)).thenReturn(responseDto);

            mockMvc.perform(get("/api/datacenters/{id}", id)).andExpect(status().isOk()).andExpectAll(jsonPath("$.id").exists(), jsonPath("$.country").exists(), jsonPath("$.address").exists(), jsonPath("$.dataCenterType").exists());

            verify(serviceMock, times(1)).getById(id);
        }

        @Test
        void should_Return404_When_ServerNotFound() throws Exception {
            int id = 1;
            when(serviceMock.getById(id)).thenThrow(DataCenterNotFoundException.class);

            mockMvc.perform(get("/api/datacenters/{id}", id)).andExpect(status().isNotFound()).andExpect(jsonPath("$.errorName").value("DataCenterNotFoundException"));

            verify(serviceMock, times(1)).getById(id);
        }

        static Stream<Arguments> invalidPathVarTestCases() {
            return Stream.of(
                    Arguments.of("adf", "MethodArgumentTypeMismatchException"),
                    Arguments.of(-1, "ConstraintViolationException"));
        }

        @ParameterizedTest
        @MethodSource("invalidPathVarTestCases")
        void should_Return400_When_InvalidId(Object param, String exception) throws Exception {

            mockMvc.perform(get("/api/datacenters/{id}", param))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName").value(exception));

            verifyNoInteractions(serviceMock);
        }
    }

    @Nested
    class GetAll {
        @Test
        void should_Return200AndList_When_DataCentersExist() throws Exception {
            when(serviceMock.getAll()).thenReturn(responses);

            mockMvc.perform(get("/api/datacenters"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpectAll(jsonPath("$[0].id").value(1), jsonPath("$[0].country").value("Egypt"), jsonPath("$[0].address").value("Test address"),
                            jsonPath("$[0].dataCenterType").value("CLOUD"))
                    .andExpectAll(jsonPath("$[1].id").value(2), jsonPath("$[1].country").value("USA"), jsonPath("$[1].address").value("Test address"),
                            jsonPath("$[1].dataCenterType").value("AI"));

            verify(serviceMock, times(1)).getAll();
        }

        @Test
        void should_Return200AndEmptyList_When_NoDataCenters() throws Exception {
            when(serviceMock.getAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/datacenters"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(0));

            verify(serviceMock, times(1)).getAll();
        }


    }

    @Nested
    class AddDataCenter {
        @BeforeEach
        void setupCreation() {
            request = new DataCenterRequestDto();
            request.setCountry("Egypt");
            request.setAddress("Test, Test, Test");
            request.setMaxNoOfServers(50);
            request.setDataCenterType(DataCenterType.AI);
        }

        @Test
        void should_Return200_When_AllValid_HappyPath() throws Exception {
            mockMvc.perform(post("/api/datacenters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(serviceMock, times(1)).create(request);
        }

        static Stream<Arguments> invalidCountryAndAddressTestCases() {
            return Stream.of(
                    Arguments.of(new DataCenterRequestDto("Indonesia", "Test, Test, Test", 50, 0, DataCenterType.AI)),
                    Arguments.of(new DataCenterRequestDto("Germany", "Test", 50, 0, DataCenterType.AI)));
        }

        @ParameterizedTest
        @MethodSource("invalidCountryAndAddressTestCases")
        void should_Return400_When_InvalidFieldsCountryAndAddress(DataCenterRequestDto dto) throws Exception {
            mockMvc.perform(post("/api/datacenters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorName").value("MethodArgumentNotValidException")).andExpect(jsonPath("$.reasons.size()").value(1));

            verifyNoInteractions(serviceMock);
        }

        @ParameterizedTest
        @CsvSource(value = {"500", "0"})
        void should_Return400_When_InvalidMaxNumberOfServers(int max) throws Exception {
            request.setMaxNoOfServers(max);
            mockMvc.perform(post("/api/datacenters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName")
                            .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.reasons.size()").value(1));

            verifyNoInteractions(serviceMock);
        }

        static Stream<Arguments> invalidDataCenterTypeTestCases() {
            return Stream.of(
                    Arguments.of((Object) null));
        }

        @ParameterizedTest
        @MethodSource("invalidDataCenterTypeTestCases")
        void should_Return400_When_NullDataCenterType(Object type) throws Exception {
            request.setDataCenterType((DataCenterType) type);
            mockMvc.perform(post("/api/datacenters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName")
                            .value("MethodArgumentNotValidException"))
                    .andExpect(jsonPath("$.reasons.size()").value(1));

            verifyNoInteractions(serviceMock);
        }
    }

    @Nested
    class UpdateDataCenter {
        private DataCenterUpdateRequestDto updateRequestDto;

        static Stream<Arguments> validUpdateRequestTestCases() {
            return Stream.of(
                    Arguments.of(DataCenterUpdateRequestDto.builder().dataCenterType(DataCenterType.AI).country("Germany").build()),
                    Arguments.of(DataCenterUpdateRequestDto.builder().maxNoOfServers(50).address("Test, Test, Test").build()));
        }

        @ParameterizedTest
        @MethodSource("validUpdateRequestTestCases")
        void should_Return2xx_When_AllValid_HappyPath(DataCenterUpdateRequestDto updateRequestDto) throws Exception {
            int id = 1;

            mockMvc.perform(put("/api/datacenters/{datacenterId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequestDto)))
                    .andExpect(status().isAccepted());

            verify(serviceMock, times(1)).update(updateRequestDto, id);
        }

        static Stream<Arguments> invalidPathVarTestCases() {
            return Stream.of(
                    Arguments.of("adf", "MethodArgumentTypeMismatchException"),
                    Arguments.of(-1, "ConstraintViolationException"));
        }

        @ParameterizedTest
        @MethodSource("invalidPathVarTestCases")
        void should_Return400_When_InvalidPathVariable(Object param, String exceptionName) throws Exception {
            updateRequestDto = new DataCenterUpdateRequestDto();
            mockMvc.perform(put("/api/datacenters/{datacenterId}", param)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName").value(exceptionName));

            verifyNoInteractions(serviceMock);
        }

        @Test
        void should_Return404_When_DataCenterNotFound() throws Exception {
            int id = 1;
            updateRequestDto = new DataCenterUpdateRequestDto();

            doThrow(DataCenterNotFoundException.class).when(serviceMock).update(updateRequestDto, id);

            mockMvc.perform(put("/api/datacenters/{datacenterId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorName").value("DataCenterNotFoundException"));

            verify(serviceMock, times(1)).update(updateRequestDto, id);
        }


        static Stream<Arguments> invalidFieldsTestCases() {
            return Stream.of(
                    Arguments.of(DataCenterUpdateRequestDto.builder().country("Indonesia").build()),
                    Arguments.of(DataCenterUpdateRequestDto.builder().address("Indonesia").build()),
                    Arguments.of(DataCenterUpdateRequestDto.builder().maxNoOfServers(1000).build()));
        }

        @ParameterizedTest
        @MethodSource("invalidFieldsTestCases")
        void should_Return400_When_InvalidFields(DataCenterUpdateRequestDto updateDto) throws Exception {
            int id = 1;
            mockMvc.perform(put("/api/datacenters/{datacenterId}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpectAll(status().isBadRequest(), (jsonPath("$.errorName")
                                    .value("MethodArgumentNotValidException")),
                            jsonPath("$.reasons.size()").value(1));

            verifyNoInteractions(serviceMock);
        }
    }

    @Nested
    class AddOrUpdateServers {
        private DataCenterServersAddOrUpdateRequestDto requestDto;

        @Test
        void should_Return200_When_AllValid_HappyPath() throws Exception {
            requestDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(1, 2, 3));
            int id = 1;

            mockMvc.perform(patch("/api/datacenters/{datacenterId}/servers", id)
                            .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isAccepted());

            verify(serviceMock, times(1)).addOrUpdateServers(requestDto, id);
        }

        static Stream<Arguments> invalidPathVarTestCases() {
            return Stream.of(
                    Arguments.of("adf", "MethodArgumentTypeMismatchException"),
                    Arguments.of(-1, "ConstraintViolationException"));
        }

        @ParameterizedTest
        @MethodSource("invalidPathVarTestCases")
        void should_Return400_When_InvalidPathVariable(Object param, String exceptionName) throws Exception {
            requestDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(1, 2, 3));

            mockMvc.perform(patch("/api/datacenters/{datacenterId}/servers", param)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName").value(exceptionName));

            verifyNoInteractions(serviceMock);
        }

        @Test
        void should_Return400_When_InvalidRequest() throws Exception {
            requestDto = new DataCenterServersAddOrUpdateRequestDto();
            int id = 1;

            mockMvc.perform(patch("/api/datacenters/{datacenterId}/servers", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorName").value("MethodArgumentNotValidException"));

            verifyNoInteractions(serviceMock);
        }

        @Test
        void should_Return404_When_ServersNotFound() throws Exception {
            requestDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(1,2));
            int id = 1;

            doThrow(NotAllServersFoundException.class).when(serviceMock).addOrUpdateServers(requestDto, id);

            mockMvc.perform(patch("/api/datacenters/{datacenterId}/servers", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorName").value("NotAllServersFoundException"));

            verify(serviceMock, times(1)).addOrUpdateServers(requestDto, id);
        }

        @Test
        void should_Return409_When_DatacenterCapacityExceeded() throws Exception {
            requestDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(1,2));
            int id = 1;

            doThrow(CurrentCapacityOverMaxCapacityException.class).when(serviceMock).addOrUpdateServers(requestDto, id);

            mockMvc.perform(patch("/api/datacenters/{datacenterId}/servers", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorName").value("CurrentCapacityOverMaxCapacityException"));

            verify(serviceMock, times(1)).addOrUpdateServers(requestDto, id);
        }

        @Test
        void should_Return404_When_DataCenterNotFound() throws Exception {
            requestDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(1,2));
            int id = 1;

            doThrow(DataCenterNotFoundException.class).when(serviceMock).addOrUpdateServers(requestDto, id);

            mockMvc.perform(patch("/api/datacenters/{datacenterId}/servers", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorName").value("DataCenterNotFoundException"));

            verify(serviceMock, times(1)).addOrUpdateServers(requestDto, id);
        }

    }

}
