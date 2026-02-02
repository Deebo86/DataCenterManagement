package com.example.Task1.service;

import com.example.Task1.dto.*;
import com.example.Task1.exception.*;
import com.example.Task1.mapper.IServerMapper;
import com.example.Task1.model.*;
import com.example.Task1.model.enums.OperatingSystem;
import com.example.Task1.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class ServerServiceTest {

    @Mock
    private ServerRepo serverRepo;
    @Mock
    private IServerMapper mapper;
    @Mock
    private DataCenterRepo dcRepo;
    @InjectMocks
    private ServerService service;

    @Nested
    class ServerCreationTests {
        @Test
        void should_CreateAndSave_When_CorrectServerRequestDtoAndMapping() throws Exception {
            //given
            ServerRequestDto dto = mock(ServerRequestDto.class);
            Server mappedServer = mock(Server.class);

            when(mapper.toServer(dto)).thenReturn(mappedServer);

            //when
            service.create(dto);

            //then
            verify(mapper, times(1)).toServer(dto);
            verify(serverRepo, times(1)).save(mappedServer);

        }

        //TODO: what happens should mapping fail? need to test for that

        @Test
        void should_InsertDataFromExcel_HappyPath() throws IOException {
            MockMultipartFile file = new MockMultipartFile("ServerTestData.xlsx", new FileInputStream("src/test/resources/ServerTestData.xlsx"));
            Server s1 = new Server();
            s1.setMemory(100);
            s1.setNoOfCPU(7);
            s1.setOs(OperatingSystem.WINDOWS);

            Server s2 = new Server();
            s2.setMemory(80);
            s2.setNoOfCPU(5);
            s2.setOs(OperatingSystem.LINUX);

            ArgumentCaptor<Server> serverCaptor = ArgumentCaptor.forClass(Server.class);

            when(serverRepo.save(any(Server.class))).thenAnswer(inv -> inv.getArgument(0));

            //when
            service.create(file);

            //then
            verify(serverRepo, times(2)).save(serverCaptor.capture());
            List<Server> captured = serverCaptor.getAllValues();

            assertAll(
                    () -> assertEquals(2, captured.size()),
                    () -> assertEquals(s1.getMemory(), captured.get(0).getMemory()),
                    () -> assertEquals(s1.getNoOfCPU(), captured.get(0).getNoOfCPU()),
                    () -> assertEquals(s1.getOs(), captured.get(0).getOs()),
                    () -> assertEquals(s2.getMemory(), captured.get(1).getMemory()),
                    () -> assertEquals(s2.getNoOfCPU(), captured.get(1).getNoOfCPU()),
                    () -> assertEquals(s2.getOs(), captured.get(1).getOs())
            );

        }

        @Test
        void should_ThrowUploadEmptyFileException_When_FileIsEmpty() throws IOException {
            MockMultipartFile file = new MockMultipartFile("ServerTestData.xlsx", mock(FileInputStream.class));

            assertThrows(UploadedEmptyFileException.class, () -> service.create(file));

        }

        //Todo: test that throws the ioexception

    }

    @Nested
    class GetByIdTests {
        @Test
        void should_ReturnAServerResponseDto_When_CorrectId() {
            //given
            int id = 1;
            Server s1 = mock(Server.class);
            ServerResponseDto dto1 = mock(ServerResponseDto.class);
//            ServerResponseDto dto1 = new ServerResponseDto(1, -1, 60, 5, OperatingSystem.LINUX);

            when(serverRepo.findById(id)).thenReturn(Optional.of(s1));
            when(mapper.toResponseDto(s1)).thenReturn(dto1);

            //when
            ServerResponseDto res = service.getById(id);

            //then
            assertAll(
                    () -> assertNotNull(res),
                    () -> assertEquals(res, dto1),
                    () -> assertNotEquals(-1, res.getDatacenterId())
            );

            verify(mapper).toResponseDto(s1);
            verify(serverRepo).findById(id);
        }

        @Test
        void should_ReturnAServerResponseDto_When_DatacenterNull() {
            //given
            int id = 1;
            Server s1 = mock(Server.class);
            ServerResponseDto spyDto = spy(ServerResponseDto.class);
//            ServerResponseDto dto1 = new ServerResponseDto(1, -1, 60, 5, OperatingSystem.LINUX);

            when(serverRepo.findById(id)).thenReturn(Optional.of(s1));
            when(mapper.toResponseDto(s1)).thenReturn(spyDto);
            when(s1.getDatacenter()).thenReturn(null);

            //when
            ServerResponseDto res = service.getById(id);

            //then
            assertAll(
                    () -> assertNotNull(res),
                    () -> assertEquals(res, spyDto),
                    () -> assertEquals(-1, res.getDatacenterId())
            );

            verify(mapper, times(1)).toResponseDto(s1);
            verify(serverRepo, times(1)).findById(id);
            verify(spyDto, times(1)).setDatacenterId(-1);
        }

        @Test
        void should_ReturnAServerResponseDto_When_DatacenterNotNull() {
            //given
            int id = 1;
            Server s1 = spy(Server.class);
            s1.setId(1);
            DataCenter d1 = spy(DataCenter.class);
            d1.setId(10);
            s1.setDatacenter(d1);
            ServerResponseDto spyDto = spy(ServerResponseDto.class);
            spyDto.setId(s1.getId());
            spyDto.setDatacenterId(d1.getId());

            when(serverRepo.findById(id)).thenReturn(Optional.of(s1));
            when(mapper.toResponseDto(s1)).thenReturn(spyDto);
//            when(s1.getDatacenter()).thenReturn(d1);

            //when
            ServerResponseDto res = service.getById(id);

            //then
            assertAll(
                    () -> assertNotNull(res),
                    () -> assertEquals(res, spyDto),
                    () -> assertEquals(d1.getId(), res.getDatacenterId())
            );

            verify(mapper, times(1)).toResponseDto(s1);
            verify(serverRepo, times(1)).findById(id);
            verify(spyDto, never()).setDatacenterId(-1);
        }

        @Test
        void should_ThrowServerNotFoundException_When_ServerDoesNotExist() {
            //given
            int id = 1;
            when(serverRepo.findById(id)).thenReturn(Optional.empty());

            //when

            //then
            assertThrows(ServerNotFoundException.class, () -> service.getById(id));

            verifyNoInteractions(mapper);
            verify(serverRepo).findById(id);
            verifyNoMoreInteractions(serverRepo);
        }
    }

    @Nested
    class GetAllTests {
        @Test
        void should_ReturnListOfServerResponseDto_When_Called() {
            //given
            Server s1 = new Server(1, null, 60, 5, OperatingSystem.LINUX);
            Server s2 = new Server(2, null, 100, 7, OperatingSystem.WINDOWS);
            Server s3 = new Server(3, null, 80, 5, OperatingSystem.UNIX);
            List<Server> servers = new ArrayList<>(List.of(s1, s2, s3));

            ServerResponseDto dto1 = new ServerResponseDto(1, -1, 60, 5, OperatingSystem.LINUX);
            ServerResponseDto dto2 = new ServerResponseDto(2, -1, 100, 7, OperatingSystem.WINDOWS);
            ServerResponseDto dto3 = new ServerResponseDto(3, -1, 80, 5, OperatingSystem.UNIX);
            List<ServerResponseDto> dtos = new ArrayList<>(List.of(dto1, dto2, dto3));

            when(serverRepo.findAll()).thenReturn(servers);
            when(mapper.toResponseDto(s1)).thenReturn(dto1);
            when(mapper.toResponseDto(s2)).thenReturn(dto2);
            when(mapper.toResponseDto(s3)).thenReturn(dto3);

            //when
            List<ServerResponseDto> res = service.getAll();

            //then
            assertIterableEquals(res, dtos);

            verify(mapper, times(3)).toResponseDto(any(Server.class));
            verify(serverRepo, times(1)).findAll();
        }

        @Test
        void should_ReturnEmptyListOfServerResponseDto_When_NoServers() {
            //given
            when(serverRepo.findAll()).thenReturn(new ArrayList<>());

            //when
            List<ServerResponseDto> res = service.getAll();

            //then
            assertEquals(0, res.size());

            verify(mapper, never()).toResponseDto(any(Server.class));
            verify(serverRepo, times(1)).findAll();
        }

        @Test
        void should_ReturnListOfServerResponseDto_When_CalledWithNullDataCenters() {
            //given
            DataCenter d1 = new DataCenter();
            d1.setId(1);
            DataCenter d2 = new DataCenter();
            d2.setId(2);
            Server s1 = new Server();
            s1.setId(1);
            Server s2 = new Server();
            s2.setId(2);
            s2.setDatacenter(d1);
            Server s3 = new Server();
            s3.setId(3);
            s3.setDatacenter(d2);
            List<Server> servers = new ArrayList<>(List.of(s1, s2, s3));

            ServerResponseDto initialDto1 = spy(ServerResponseDto.class);
            initialDto1.setId(1);

            ServerResponseDto initialDto2 = spy(ServerResponseDto.class);
            initialDto2.setId(2);
            initialDto2.setDatacenterId(1);

            ServerResponseDto initialDto3 = spy(ServerResponseDto.class);
            initialDto3.setId(3);
            initialDto3.setDatacenterId(2);

            ServerResponseDto expectedDto1 = spy(ServerResponseDto.class);
            expectedDto1.setId(1);
            expectedDto1.setDatacenterId(-1);

            ServerResponseDto expectedDto2 = spy(ServerResponseDto.class);
            expectedDto2.setId(2);
            expectedDto2.setDatacenterId(1);

            ServerResponseDto expectedDto3 = spy(ServerResponseDto.class);
            expectedDto3.setId(3);
            expectedDto3.setDatacenterId(2);

            List<ServerResponseDto> dtos = new ArrayList<>(List.of(expectedDto1, expectedDto2, expectedDto3));

            when(serverRepo.findAll()).thenReturn(servers);
            when(mapper.toResponseDto(s1)).thenReturn(initialDto1);
            when(mapper.toResponseDto(s2)).thenReturn(initialDto2);
            when(mapper.toResponseDto(s3)).thenReturn(initialDto3);

            //when
            List<ServerResponseDto> res = service.getAll();

            //then
            assertAll(
                    () -> assertEquals(dtos.getFirst().getDatacenterId(), res.getFirst().getDatacenterId()),
                    () -> assertEquals(dtos.get(1).getDatacenterId(), res.get(1).getDatacenterId()),
                    () -> assertEquals(dtos.get(2).getDatacenterId(), res.get(2).getDatacenterId())
            );

            verify(mapper, times(3)).toResponseDto(any(Server.class));
            verify(serverRepo, times(1)).findAll();
        }

    }

    @Nested
    class UpdateTest {
        @Test
        void should_UpdateServer() {
            //same entity as the input as the output

            //given
            int id = 1;
            DataCenter dc = null;
            int memory = 60;
            int noOfCpu = 5;
            OperatingSystem osExisting = OperatingSystem.LINUX;
            OperatingSystem osNew = OperatingSystem.WINDOWS;

            Server s1 = new Server(id, dc, memory, noOfCpu, osExisting);
            ServerUpdateRequestDto dto = new ServerUpdateRequestDto();
            dto.setOs(osNew);

            when(serverRepo.findById(id)).thenReturn(Optional.of(s1));

            //when
            service.update(dto, 1);

            //then
            verify(mapper, times(1)).updateServerFromDto(dto, s1);
            verify(serverRepo, times(1)).findById(id);
            verify(serverRepo, times(1)).save(s1);
        }

        @Test
        void should_ThrowException_When_ServerNotFound() {
            //given
            int id = 1;

            when(serverRepo.findById(id)).thenReturn(Optional.empty());

            //then
            assertThrows(ServerNotFoundException.class, () -> service.update(any(ServerUpdateRequestDto.class), id));
            verify(serverRepo, times(1)).findById(id);
            verifyNoInteractions(mapper);
            verify(serverRepo, never()).save(any(Server.class));
        }
    }

    @Nested
    class DeleteTest {
        @Test
        void should_Delete_WhenTrue() {
            int id = 1;
            when(serverRepo.existsById(id)).thenReturn(true);

            service.delete(id);

            verify(serverRepo, times(1)).existsById(id);
            verify(serverRepo, times(1)).deleteById(id);
            verifyNoMoreInteractions(serverRepo);
            verifyNoInteractions(mapper);
            verifyNoInteractions(dcRepo);
        }

        @Test
        void should_Delete_WhenFalse() {
            int id = 1;
            when(serverRepo.existsById(id)).thenReturn(false);

            assertThrows(ServerNotFoundException.class, () -> service.delete(id));

            verify(serverRepo, times(1)).existsById(id);
            verify(serverRepo, never()).deleteById(id);
            verifyNoMoreInteractions(serverRepo);
            verifyNoInteractions(mapper);
            verifyNoInteractions(dcRepo);
        }
    }

    @Nested
    class DownloadTests {
        private static List<ServerResponseDto> serverDtosMocks;

        private static List<Server> serverMocks;

        private static DataCenter dummyDc;

        @BeforeAll
        static void testDataSetup() {
            serverDtosMocks = Arrays.asList(
                    new ServerResponseDto(1, 1, 100, 7, OperatingSystem.LINUX),
                    new ServerResponseDto(2, 1, 80, 2, OperatingSystem.WINDOWS)
            );

            dummyDc = new DataCenter();
            dummyDc.setId(1);
            serverMocks = Arrays.asList(
                    new Server(1, dummyDc, 100, 7, OperatingSystem.LINUX),
                    new Server(2, dummyDc, 80, 2, OperatingSystem.WINDOWS)
            );
        }

        @Test
        void should_GenerateExcelWithCorrectData_When_HappyPath() throws IOException {
            DataCenter d = new DataCenter();
            d.setId(1);
            when(serverRepo.findAll()).thenReturn(serverMocks);
            when(mapper.toResponseDto(any(Server.class))).thenReturn(serverDtosMocks.get(0)).thenReturn(serverDtosMocks.get(1));

            ByteArrayInputStream input = service.download();
            assertNotNull(input, "download() returns an InputStream object that has some content");

            try (Workbook book = new XSSFWorkbook(input)) {
                Sheet sheet = book.getSheet("ServerData");
                assertNotNull(sheet, "Check to ensure an excel sheet called ServerData existed in the InputStream");

                Row head = sheet.getRow(0);
                assertAll(
                        () -> assertNotNull(head, "Checks header contains data and mark the start of the row value checks"),
                        () -> assertEquals("Id", head.getCell(0).getStringCellValue()),
                        () -> assertEquals("DataCenter Id", head.getCell(1).getStringCellValue()),
                        () -> assertEquals("Memory", head.getCell(2).getStringCellValue()),
                        () -> assertEquals("NoOfCPUs", head.getCell(3).getStringCellValue()),
                        () -> assertEquals("OS", head.getCell(4).getStringCellValue())
                );

                Row r1 = sheet.getRow(1);
                assertAll(
                        () -> assertNotNull(r1, "Checks first row contains data and mark the start of the row value checks"),
                        () -> assertEquals(1, r1.getCell(0).getNumericCellValue()),
                        () -> assertEquals(1, r1.getCell(1).getNumericCellValue()),
                        () -> assertEquals(100, r1.getCell(2).getNumericCellValue()),
                        () -> assertEquals(7, r1.getCell(3).getNumericCellValue()),
                        () -> assertEquals(OperatingSystem.LINUX.name(), r1.getCell(4).getStringCellValue())
                );

                Row r2 = sheet.getRow(2);
                assertAll(
                        () -> assertNotNull(r2, "Checks second row contains data and mark the start of the row value checks"),
                        () -> assertEquals(2, r2.getCell(0).getNumericCellValue()),
                        () -> assertEquals(1, r2.getCell(1).getNumericCellValue()),
                        () -> assertEquals(80, r2.getCell(2).getNumericCellValue()),
                        () -> assertEquals(2, r2.getCell(3).getNumericCellValue()),
                        () -> assertEquals(OperatingSystem.WINDOWS.name(), r2.getCell(4).getStringCellValue())
                );

                assertNull(sheet.getRow(3), "Checks sheet contains only the rows it should and nothing more");

            }
        }

        @Test
        void should_HandleWell_When_NoServers() throws IOException {
            when(serverRepo.findAll()).thenReturn(Collections.emptyList());

            ByteArrayInputStream input = service.download();
            assertNotNull(input, "download() returns an InputStream object that has some content");

            try (Workbook book = new XSSFWorkbook(input)) {
                Sheet sheet = book.getSheet("ServerData");
                assertNotNull(sheet, "Check to ensure an excel sheet called ServerData existed in the InputStream");

                Row head = sheet.getRow(0);
                assertAll(
                        () -> assertNotNull(head, "Checks header contains data and mark the start of the row value checks"),
                        () -> assertEquals("Id", head.getCell(0).getStringCellValue()),
                        () -> assertEquals("DataCenter Id", head.getCell(1).getStringCellValue()),
                        () -> assertEquals("Memory", head.getCell(2).getStringCellValue()),
                        () -> assertEquals("NoOfCPUs", head.getCell(3).getStringCellValue()),
                        () -> assertEquals("OS", head.getCell(4).getStringCellValue())
                );

                assertNull(sheet.getRow(1), "There should be no more rows in the sheet");
            }
        }

        //TODO: How can I check on this IO exception?

    }

}