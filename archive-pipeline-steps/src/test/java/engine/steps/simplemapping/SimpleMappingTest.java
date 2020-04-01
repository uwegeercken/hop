/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.apache.hop.pipeline.steps.simplemapping;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.pipeline.RowProducer;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.step.StepInterface;
import org.apache.hop.pipeline.steps.mapping.MappingIODefinition;
import org.apache.hop.pipeline.steps.mappinginput.MappingInput;
import org.apache.hop.pipeline.steps.mappingoutput.MappingOutput;
import org.apache.hop.pipeline.steps.mock.StepMockHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tatsiana_Kasiankova
 */
public class SimpleMappingTest {

  private static final String MAPPING_INPUT_STEP_NAME = "MAPPING_INPUT_STEP_NAME";

  private static final String MAPPING_OUTPUT_STEP_NAME = "MAPPING_OUTPUT_STEP_NAME";

  private StepMockHelper<SimpleMappingMeta, SimpleMappingData> stepMockHelper;

  // Using real SimpleMappingData object
  private SimpleMappingData simpleMpData = new SimpleMappingData();

  private SimpleMapping smp;

  @Before
  public void setup() throws Exception {
    stepMockHelper =
      new StepMockHelper<SimpleMappingMeta, SimpleMappingData>( "SIMPLE_MAPPING_TEST", SimpleMappingMeta.class,
        SimpleMappingData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    when( stepMockHelper.pipeline.isRunning() ).thenReturn( true );

    // Mock for MappingInput
    MappingInput mpInputMock = mock( MappingInput.class );
    when( mpInputMock.getStepname() ).thenReturn( MAPPING_INPUT_STEP_NAME );

    // Mock for MappingOutput
    MappingOutput mpOutputMock = mock( MappingOutput.class );
    when( mpOutputMock.getStepname() ).thenReturn( MAPPING_OUTPUT_STEP_NAME );

    // Mock for RowDataInputMapper
    RowDataInputMapper rdInputMpMock = mock( RowDataInputMapper.class );
    RowMetaInterface rwMetaInMock = mock( RowMeta.class );
    doReturn( Boolean.TRUE ).when( rdInputMpMock ).putRow( rwMetaInMock, new Object[] {} );

    // Mock for RowProducer
    RowProducer rProducerMock = mock( RowProducer.class );
    when( rProducerMock.putRow( any( RowMetaInterface.class ), any( Object[].class ), anyBoolean() ) )
      .thenReturn( true );

    // Mock for MappingIODefinition
    MappingIODefinition mpIODefMock = mock( MappingIODefinition.class );

    // Set up real SimpleMappingData with some mocked elements
    simpleMpData.mappingInput = mpInputMock;
    simpleMpData.mappingOutput = mpOutputMock;
    simpleMpData.rowDataInputMapper = rdInputMpMock;
    simpleMpData.mappingPipeline = stepMockHelper.pipeline;

    when( stepMockHelper.pipeline.findStepInterface( MAPPING_OUTPUT_STEP_NAME, 0 ) ).thenReturn( mpOutputMock );
    when( stepMockHelper.pipeline.addRowProducer( MAPPING_INPUT_STEP_NAME, 0 ) ).thenReturn( rProducerMock );
    when( stepMockHelper.processRowsStepMetaInterface.getInputMapping() ).thenReturn( mpIODefMock );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testStepSetUpAsWasStarted_AtProcessingFirstRow() throws HopException {

    smp =
      new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.pipelineMeta,
        stepMockHelper.pipeline );
    smp.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    smp.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
    assertTrue( "The step is processing in first", smp.first );
    assertTrue( smp.processRow( stepMockHelper.processRowsStepMetaInterface, simpleMpData ) );
    assertFalse( "The step is processing not in first", smp.first );
    assertTrue( "The step was started", smp.getData().wasStarted );

  }

  @Test
  public void testStepShouldProcessError_WhenMappingPipelineHasError() throws HopException {

    // Set Up TransMock to return the error
    int errorCount = 1;
    when( stepMockHelper.pipeline.getErrors() ).thenReturn( errorCount );

    // The step has been already finished
    when( stepMockHelper.pipeline.isFinished() ).thenReturn( Boolean.TRUE );
    // The step was started
    simpleMpData.wasStarted = true;

    smp =
      new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.pipelineMeta,
        stepMockHelper.pipeline );
    smp.init( stepMockHelper.initStepMetaInterface, simpleMpData );

    smp.dispose( stepMockHelper.processRowsStepMetaInterface, simpleMpData );
    verify( stepMockHelper.pipeline, times( 1 ) ).isFinished();
    verify( stepMockHelper.pipeline, never() ).waitUntilFinished();
    verify( stepMockHelper.pipeline, never() ).addActiveSubPipelineformation( anyString(), any( Pipeline.class ) );
    verify( stepMockHelper.pipeline, times( 1 ) ).removeActiveSubPipelineformation( anyString() );
    verify( stepMockHelper.pipeline, never() ).getActiveSubPipelineformation( anyString() );
    verify( stepMockHelper.pipeline, times( 1 ) ).getErrors();
    assertTrue( "The step contains the errors", smp.getErrors() == errorCount );

  }

  @Test
  public void testStepShouldStopProcessingInput_IfUnderlyingTransitionIsStopped() throws Exception {

    MappingInput mappingInput = mock( MappingInput.class );
    when( mappingInput.getStepname() ).thenReturn( MAPPING_INPUT_STEP_NAME );
    stepMockHelper.processRowsStepDataInterface.mappingInput = mappingInput;

    RowProducer rowProducer = mock( RowProducer.class );
    when( rowProducer.putRow( any( RowMetaInterface.class ), any( Object[].class ), anyBoolean() ) )
      .thenReturn( true );

    StepInterface stepInterface = mock( StepInterface.class );

    Pipeline mappingPipeline = mock( Pipeline.class );
    when( mappingPipeline.addRowProducer( anyString(), anyInt() ) ).thenReturn( rowProducer );
    when( mappingPipeline.findStepInterface( anyString(), anyInt() ) ).thenReturn( stepInterface );
    when( mappingPipeline.isFinishedOrStopped() ).thenReturn( Boolean.FALSE ).thenReturn( Boolean.TRUE );
    stepMockHelper.processRowsStepDataInterface.mappingPipeline = mappingPipeline;

    MappingOutput mappingOutput = mock( MappingOutput.class );
    when( mappingOutput.getStepname() ).thenReturn( MAPPING_OUTPUT_STEP_NAME );
    stepMockHelper.processRowsStepDataInterface.mappingOutput = mappingOutput;


    smp = new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.pipelineMeta,
      stepMockHelper.pipeline );
    smp.init( stepMockHelper.initStepMetaInterface, simpleMpData );
    smp.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );
    smp.addRowSetToInputRowSets( stepMockHelper.getMockInputRowSet( new Object[] {} ) );

    assertTrue(
      smp.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );
    assertFalse(
      smp.processRow( stepMockHelper.processRowsStepMetaInterface, stepMockHelper.processRowsStepDataInterface ) );

  }

  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testDispose() throws HopException {

    // Set Up TransMock to return the error
    when( stepMockHelper.pipeline.getErrors() ).thenReturn( 0 );

    // The step has been already finished
    when( stepMockHelper.pipeline.isFinished() ).thenReturn( Boolean.FALSE );
    // The step was started
    simpleMpData.wasStarted = true;

    smp =
      new SimpleMapping( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.pipelineMeta,
        stepMockHelper.pipeline );
    smp.init( stepMockHelper.initStepMetaInterface, simpleMpData );

    smp.dispose( stepMockHelper.processRowsStepMetaInterface, simpleMpData );
    verify( stepMockHelper.pipeline, times( 1 ) ).isFinished();
    verify( stepMockHelper.pipeline, times( 1 ) ).waitUntilFinished();
    verify( stepMockHelper.pipeline, times( 1 ) ).removeActiveSubPipelineformation( anyString() );

  }

}