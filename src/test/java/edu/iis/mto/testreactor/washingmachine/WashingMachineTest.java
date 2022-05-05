package edu.iis.mto.testreactor.washingmachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WashingMachineTest {

    @Mock
    private DirtDetector dirtDetector;
    @Mock
    private Engine engine;
    @Mock
    private WaterPump waterPump;

    private WashingMachine washingMachine;

    @BeforeEach
    void setUp() throws Exception {
        dirtDetector = Mockito.mock(DirtDetector.class);
        engine = Mockito.mock(Engine.class);
        waterPump = Mockito.mock(WaterPump.class);
        washingMachine = new WashingMachine(dirtDetector, engine, waterPump);
    }

    @Test
    void test() {
        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(5)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.LONG)
                .build();

        LaundryStatus expectedLaundryStatus = LaundryStatus.builder()
                .withErrorCode(ErrorCode.NO_ERROR)
                .withResult(Result.SUCCESS)
                .withRunnedProgram(Program.LONG)
                .build();

        LaundryStatus actualLaundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertEquals(expectedLaundryStatus, actualLaundryStatus);
    }

    @Test
    void noSpinProgramLongBehaviorTest() throws WaterPumpException, EngineException {
        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(5)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(false)
                .withProgram(Program.LONG)
                .build();

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(programConfiguration.getProgram().getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(0)).spin();

    }

    @Test
    void spinProgramLongBehaviorTest() throws WaterPumpException, EngineException {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(5)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.AUTODETECT)
                .build();

        Mockito.when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(0));

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(dirtDetector, Mockito.times(1)).detectDirtDegree(laundryBatch);
        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.MEDIUM.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

}
