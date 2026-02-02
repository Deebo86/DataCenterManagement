package com.example.Task1.service;

import com.example.Task1.dto.*;
import com.example.Task1.exception.*;
import com.example.Task1.mapper.IDataCenterMapper;
import com.example.Task1.model.*;
import com.example.Task1.model.enums.*;
import com.example.Task1.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataCenterServiceTest {

    @Mock
    private DataCenterRepo dcRepo;
    @Mock
    private IDataCenterMapper mapper;
    @Mock
    private ServerRepo serverRepo;
    @InjectMocks
    private DataCenterService service;

    private DataCenter mockDc;
    private DataCenterResponseDto mockResponseDto;
    private List<DataCenterResponseDto> mockResponseDtoList;
    private List<DataCenter> mockDcList;

    @BeforeEach
    void setUp() {
        mockDc = spy(DataCenter.class);
        mockDc.setId(1);
        mockDc.setCountry("Egypt");
        mockDc.setMaxNoOfServers(3);
        mockDc.setDataCenterType(DataCenterType.AI);

        DataCenter d1 = spy(DataCenter.class);
        d1.setId(2);
        d1.setCountry("USA");
        d1.setMaxNoOfServers(7);
        d1.setDataCenterType(DataCenterType.CLOUD);

        DataCenter d2 = spy(DataCenter.class);
        d2.setId(3);
        d2.setCountry("Germany");
        d2.setMaxNoOfServers(3);
        d2.setDataCenterType(DataCenterType.ENTERPRISE);

        mockDcList = Arrays.asList(
                mockDc, d1, d2
        );

        mockResponseDto = spy(DataCenterResponseDto.class);
        mockResponseDto.setId(1);
        mockResponseDto.setCountry("Egypt");
        mockResponseDto.setMaxNoOfServers(3);
        mockResponseDto.setDataCenterType(DataCenterType.AI);

        DataCenterResponseDto dto1 = spy(DataCenterResponseDto.class);
        dto1.setId(2);
        dto1.setCountry("USA");
        dto1.setMaxNoOfServers(7);
        dto1.setDataCenterType(DataCenterType.CLOUD);

        DataCenterResponseDto dto2 = spy(DataCenterResponseDto.class);
        dto2.setId(3);
        dto2.setCountry("Germany");
        dto2.setMaxNoOfServers(3);
        dto2.setDataCenterType(DataCenterType.ENTERPRISE);

        mockResponseDtoList = Arrays.asList(
                mockResponseDto, dto1, dto2
        );

    }

    @Nested
    class GetByIdTests {

        @Test
        void should_Return_DataCenterResponseDto_When_DataCenterExists() {
            //given
            int id = 1;
            DataCenter dc = mock(DataCenter.class);

            when(dcRepo.findById(id)).thenReturn(Optional.of(dc));
            when(mapper.toDataCenterResponseDto(dc)).thenReturn(mockResponseDto);

            //when
            DataCenterResponseDto res = service.getById(1);

            //then
            assertAll(
                    () -> assertEquals(mockResponseDto.getCountry(), res.getCountry()),
                    () -> assertEquals(mockResponseDto.getMaxNoOfServers(), res.getMaxNoOfServers()),
                    () -> assertEquals(mockResponseDto.getDataCenterType(), res.getDataCenterType())
            );
            verify(dcRepo, times(1)).findById(id);
            verify(mapper, times(1)).toDataCenterResponseDto(dc);
        }

        @Test
        void should_ThrowDataCenterNotFoundException_When_DataCenterNotExist() {
            when(dcRepo.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(DataCenterNotFoundException.class, () -> service.getById(anyInt()));
            verify(dcRepo, times(1)).findById(anyInt());
            verifyNoInteractions(mapper);
        }
    }

    @Nested
    class GetAllTests {

        @Test
        void should_ReturnListOfDataCenterResponseDtos_When_DatacentersExist() {
            when(dcRepo.findAll()).thenReturn(mockDcList);
            when(mapper.toDataCenterResponseDto(mockDcList)).thenReturn(mockResponseDtoList);

            List<DataCenterResponseDto> res = service.getAll();

            assertEquals(mockResponseDtoList.size(), res.size());
            verify(dcRepo, times(1)).findAll();
            verify(mapper, times(1)).toDataCenterResponseDto(mockDcList);
        }

        @Test
        void should_ReturnEmptyList_When_NoDatacenters() {
            when(dcRepo.findAll()).thenReturn(new ArrayList<>());


            List<DataCenterResponseDto> res = service.getAll();

            assertEquals(0, res.size());
            verify(dcRepo, times(1)).findAll();
            verifyNoInteractions(mapper);

        }
    }

    @Nested
    class CreateTests {

        @Test
        void should_CreateDC_HappyPath() {
            DataCenterRequestDto request = mock(DataCenterRequestDto.class);
            when(mapper.toDataCenter(request)).thenReturn(mockDc);

            service.create(request);

            verify(mapper, times(1)).toDataCenter(any(DataCenterRequestDto.class));
            verify(dcRepo, times(1)).save(mockDc);
        }

        //Todo: mockMapper malfunctions ie. returns an object with defaults. should not save. Test
    }

    @Nested
    class DeleteTests {
        private Server s1;
        private Server s2;

        @BeforeEach
        void deleteTestSetup() {
            s1 = new Server(1, mockDc, 60, 5, OperatingSystem.LINUX);
            s2 = new Server(2, mockDc, 100, 7, OperatingSystem.WINDOWS);

        }

        @Test
        void should_DeleteDataCenter_HappyPath() {
            int id = 1;
            when(dcRepo.findById(id)).thenReturn(Optional.of(mockDc));

            List<Server> servers = Arrays.asList(s1, s2);
            mockDc.setServers(servers);
            doReturn(servers).when(mockDc).getServers();

            ArgumentCaptor<List<Server>> serversCaptor = ArgumentCaptor.forClass(List.class);
            when(serverRepo.saveAll(servers)).thenAnswer(inv -> inv.getArgument(0));

            service.delete(id);

            //then
            verify(dcRepo, times(1)).findById(anyInt());
            verify(dcRepo, times(1)).deleteById(anyInt());
            verify(serverRepo, times(1)).saveAll(serversCaptor.capture());
            List<Server> captured = serversCaptor.getValue();
            assertAll(
                    () -> assertNull(captured.getFirst().getDatacenter()),
                    () -> assertNull(captured.get(1).getDatacenter()),
                    () -> assertEquals(servers.size(), captured.size())
            );
            verifyNoInteractions(mapper);

        }

        @Test
        void should_ThrowDataCenterNotFoundException_When_DataCenterNotExist() {
            when(dcRepo.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(DataCenterNotFoundException.class, () -> service.delete(anyInt()));

            verify(dcRepo, times(1)).findById(anyInt());
            verifyNoInteractions(serverRepo);
            verifyNoMoreInteractions(dcRepo);
        }

        @Test
        void EmptyListOfServers() {
            int id = 1;
            when(dcRepo.findById(id)).thenReturn(Optional.of(mockDc));

            doReturn(Collections.emptyList()).when(mockDc).getServers();

            service.delete(id);

            //then
            verify(dcRepo, times(1)).findById(anyInt());
            verify(dcRepo, times(1)).deleteById(anyInt());
            verifyNoInteractions(mapper);
            verifyNoInteractions(serverRepo);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void should_UpdateDataCenter_HappyPath() {
            //given
            DataCenterType newType = DataCenterType.EDGE;
            DataCenterUpdateRequestDto requestDto = new DataCenterUpdateRequestDto();
            requestDto.setDataCenterType(newType);

            when(dcRepo.findById(anyInt())).thenReturn(Optional.of(mockDc));

            service.update(requestDto, anyInt());

            verify(mapper, times(1)).updateDataCenterFromDto(requestDto, mockDc);
            verify(dcRepo, times(1)).save(any(DataCenter.class));
            verify(dcRepo, times(1)).findById(anyInt());
        }

        @Test
        void should_ThrowException_When_DatacenterNotFound() {
            //given
            DataCenterUpdateRequestDto dto = mock(DataCenterUpdateRequestDto.class);
            when(dcRepo.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(DataCenterNotFoundException.class,
                    () -> service.
                            update(dto, anyInt()));

            verifyNoInteractions(mapper);
            verify(dcRepo, never()).save(any(DataCenter.class));
            verify(dcRepo, times(1)).findById(anyInt());
        }
    }

    @Nested
    class AddOrUpdateServersTests{
        private Server s1;
        private Server s2;

        @BeforeEach
        void setup() {
            s1 = new Server(1, null, 60, 5, OperatingSystem.LINUX);
            s2 = new Server(2, null, 100, 7, OperatingSystem.WINDOWS);
        }

        @Test
        void should_AddServersToDataCenter_HappyPath()
        {
            //given
            List<Server> servers = Arrays.asList(s1, s2);
            DataCenterServersAddOrUpdateRequestDto reqDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(s1.getId(), s2.getId()));
            when(dcRepo.findById(anyInt())).thenReturn(Optional.of(mockDc));
            when(serverRepo.findAllById(Arrays.asList(s1.getId(), s2.getId()))).thenReturn(servers);

            ArgumentCaptor<List<Server>> serverCap = ArgumentCaptor.forClass(List.class);
            when(serverRepo.saveAll(anyIterable())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<DataCenter> dcCap = ArgumentCaptor.forClass(DataCenter.class);
            when(dcRepo.save(any(DataCenter.class))).thenAnswer(inv -> inv.getArgument(0));

            //then
            service.addOrUpdateServers(reqDto, 1);

            //when
            verify(serverRepo, times(1)).saveAll(serverCap.capture());
            List<Server> capturedServers = serverCap.getValue();
            verify(dcRepo, times(1)).save(dcCap.capture());
            DataCenter capturedDc = dcCap.getValue();
            verifyNoInteractions(mapper);

            assertAll(
                    () -> assertSame(mockDc, capturedServers.getFirst().getDatacenter()),
                    () -> assertSame(mockDc, capturedServers.get(1).getDatacenter()),
                    () -> assertEquals(servers.size(), capturedServers.size()),
                    () -> assertEquals(servers.size(), capturedDc.getServers().size()),
                    () -> assertIterableEquals(capturedServers, capturedDc.getServers())
            );
        }

        @Test
        void should_ThrowDataCenterNotFoundException_When_DataCenterNotExist()
        {
            DataCenterServersAddOrUpdateRequestDto reqDto = mock(DataCenterServersAddOrUpdateRequestDto.class);
            int id = 1;
            when(dcRepo.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(DataCenterNotFoundException.class, () -> service.addOrUpdateServers(reqDto, id));

            verify(dcRepo, times(1)).findById(anyInt());
            verifyNoMoreInteractions(dcRepo);
            verifyNoInteractions(serverRepo);
            verifyNoInteractions(mapper);
        }

        @Test
        void should_ThrowCurrentCapacityOVerMaxCapacityException_When_CapacityLimitReached(){
            int id = 1;
            DataCenterServersAddOrUpdateRequestDto reqDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(s1.getId(), s2.getId()));
            mockDc.setMaxNoOfServers(1);

            when(dcRepo.findById(anyInt())).thenReturn(Optional.of(mockDc));

            assertThrows(CurrentCapacityOverMaxCapacityException.class,
                    () -> service.addOrUpdateServers(reqDto, id));

            verify(dcRepo, times(1)).findById(anyInt());
            verifyNoMoreInteractions(dcRepo);
            verifyNoInteractions(mapper);
            verifyNoInteractions(serverRepo);
        }

        @Test
        void should_NotAllServersFoundException_When_NotAllServersExist()
        {
            int id = 1;
            DataCenterServersAddOrUpdateRequestDto reqDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(s1.getId(), s2.getId()));
            List<Server> servers = Collections.singletonList(s1);
            when(dcRepo.findById(anyInt())).thenReturn(Optional.of(mockDc));
            when(serverRepo.findAllById(anyIterable())).thenReturn(servers);

            assertThrows(NotAllServersFoundException.class,
                    () -> service.addOrUpdateServers(reqDto, id));
            verify(dcRepo, times(1)).findById(anyInt());
            verify(serverRepo, times(1)).findAllById(anyIterable());
            verifyNoMoreInteractions(serverRepo);
            verifyNoMoreInteractions(dcRepo);
            verifyNoInteractions(mapper);

        }

        @Test
        void should_NotAllServersFoundException_When_NoServersExist()
        {
            int id = 1;
            DataCenterServersAddOrUpdateRequestDto reqDto = new DataCenterServersAddOrUpdateRequestDto(Arrays.asList(s1.getId(), s2.getId()));
            when(dcRepo.findById(anyInt())).thenReturn(Optional.of(mockDc));
            when(serverRepo.findAllById(anyIterable())).thenReturn(Collections.emptyList());

            assertThrows(NotAllServersFoundException.class,
                    () -> service.addOrUpdateServers(reqDto, id));
            verify(dcRepo, times(1)).findById(anyInt());
            verify(serverRepo, times(1)).findAllById(anyIterable());
            verifyNoMoreInteractions(serverRepo);
            verifyNoMoreInteractions(dcRepo);
            verifyNoInteractions(mapper);
        }
    }
}