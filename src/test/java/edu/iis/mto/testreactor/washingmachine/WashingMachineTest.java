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
    void setUp(){
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
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.LONG.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(0)).spin();
        assertEquals(Program.LONG.getTimeInMinutes(), programConfiguration.getProgram().getTimeInMinutes());
    }

    @Test
    void spinProgramMediumBehaviorTest() throws WaterPumpException, EngineException {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(5)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.MEDIUM)
                .build();

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.MEDIUM.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
        assertEquals(Program.MEDIUM.getTimeInMinutes(), programConfiguration.getProgram().getTimeInMinutes());
    }

    @Test
    void spinProgramShortBehaviorTest() throws WaterPumpException, EngineException {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(5)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.SHORT)
                .build();

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
        assertEquals(Program.SHORT.getTimeInMinutes(), programConfiguration.getProgram().getTimeInMinutes());
    }

    @Test
    void spinProgramAutodetectBehaviorZeroPercentTest() throws WaterPumpException, EngineException {

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

    @Test
    void spinProgramAutodetectBehaviorFiftyPercentTest() throws WaterPumpException, EngineException {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(5)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.AUTODETECT)
                .build();

        Mockito.when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(50));

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(dirtDetector, Mockito.times(1)).detectDirtDegree(laundryBatch);
        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.MEDIUM.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramAutodetectBehaviorSeventyPercentTest() throws WaterPumpException, EngineException {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(5)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.AUTODETECT)
                .build();

        Mockito.when(dirtDetector.detectDirtDegree(laundryBatch)).thenReturn(new Percentage(70));

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(dirtDetector, Mockito.times(1)).detectDirtDegree(laundryBatch);
        Mockito.verify(waterPump, Mockito.times(1)).pour(5);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.LONG.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramShortZeroWeightBehaviorTwentyPercentTest() throws WaterPumpException, EngineException {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(0)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.SHORT)
                .build();

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(0);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramOverweightBehaviorTwentyPercentTest() {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(10)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.SHORT)
                .build();

        LaundryStatus expectedLaundryStatus = LaundryStatus.builder()
                .withErrorCode(ErrorCode.TOO_HEAVY)
                .withRunnedProgram(null)
                .withResult(Result.FAILURE)
                .build();

        LaundryStatus actualLaundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertEquals(expectedLaundryStatus, actualLaundryStatus);
    }

    @Test
    void spinProgramShortNegativeWeightBehaviorTwentyPercentTest() throws WaterPumpException, EngineException { //is it ok?

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(-4)
                .withMaterialType(Material.COTTON)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.SHORT)
                .build();

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(-4);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramLongLessThanHalfWeightSpecialMaterialBehaviorTwentyPercentTest() throws WaterPumpException, EngineException {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(3)
                .withMaterialType(Material.JEANS)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.SHORT)
                .build();

        washingMachine.start(laundryBatch, programConfiguration);

        Mockito.verify(waterPump, Mockito.times(1)).pour(3);
        Mockito.verify(waterPump, Mockito.times(1)).release();
        Mockito.verify(engine, Mockito.times(1)).runWashing(Program.SHORT.getTimeInMinutes());
        Mockito.verify(engine, Mockito.times(1)).spin();
    }

    @Test
    void spinProgramLongHalfWeightSpecialMaterialBehaviorTwentyPercentTest() {

        LaundryBatch laundryBatch = LaundryBatch.builder()
                .withWeightKg(4)
                .withMaterialType(Material.JEANS)
                .build();

        ProgramConfiguration programConfiguration = ProgramConfiguration.builder()
                .withSpin(true)
                .withProgram(Program.SHORT)
                .build();

        LaundryStatus expectedLaundryStatus = LaundryStatus.builder()
                .withErrorCode(ErrorCode.TOO_HEAVY)
                .withRunnedProgram(null)
                .withResult(Result.FAILURE)
                .build();

        LaundryStatus actualLaundryStatus = washingMachine.start(laundryBatch, programConfiguration);
        assertEquals(expectedLaundryStatus, actualLaundryStatus);
    }


}
