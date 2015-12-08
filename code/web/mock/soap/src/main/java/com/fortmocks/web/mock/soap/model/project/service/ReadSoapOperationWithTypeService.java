/*
 * Copyright 2015 Karl Dahlgren
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fortmocks.web.mock.soap.model.project.service;

import com.fortmocks.core.basis.model.Service;
import com.fortmocks.core.basis.model.ServiceResult;
import com.fortmocks.core.basis.model.ServiceTask;
import com.fortmocks.core.basis.model.project.domain.Project;
import com.fortmocks.core.basis.model.project.dto.ProjectDto;
import com.fortmocks.core.mock.soap.model.project.domain.*;
import com.fortmocks.core.mock.soap.model.project.dto.SoapOperationDto;
import com.fortmocks.core.mock.soap.model.project.service.message.input.ReadSoapOperationWithTypeInput;
import com.fortmocks.core.mock.soap.model.project.service.message.output.ReadSoapOperationWithTypeOutput;

import java.util.List;

/**
 * @author Karl Dahlgren
 * @since 1.0
 */
@org.springframework.stereotype.Service
public class ReadSoapOperationWithTypeService extends AbstractSoapProjectService implements Service<ReadSoapOperationWithTypeInput, ReadSoapOperationWithTypeOutput> {

    /**
     * The process message is responsible for processing an incoming serviceTask and generate
     * a response based on the incoming serviceTask input
     * @param serviceTask The serviceTask that will be processed by the service
     * @return A result based on the processed incoming serviceTask
     * @see ServiceTask
     * @see ServiceResult
     */
    @Override
    public ServiceResult<ReadSoapOperationWithTypeOutput> process(final ServiceTask<ReadSoapOperationWithTypeInput> serviceTask) {
        final ReadSoapOperationWithTypeInput input = serviceTask.getInput();
        final SoapProject project= findType(input.getSoapProjectId());
        SoapOperationDto soapOperationDto = null;
        for(SoapPort soapPort : project.getSoapPorts()){
            if(soapPort.getUrlPath().equals(input.getUri())){
                soapOperationDto = findSoapOperation(soapPort, input.getSoapOperationMethod(), input.getType(), input.getName());
                break;
            }
        }

        return createServiceResult(new ReadSoapOperationWithTypeOutput(soapOperationDto));
    }

    /**
     * The method finds a specific SOAP operation in a SOAP port and with specific attributes
     * @param soapPort The SOAP port that is responsible for the SOAP operation
     * @param soapOperationMethod The SOAP operation method
     * @param soapOperationType The SOAP operation type
     * @param soapOperationName The SOAP operation name
     * @return The SOAP operation that matches the search criteria. Null otherwise
     */
    private SoapOperationDto findSoapOperation(SoapPort soapPort, SoapOperationMethod soapOperationMethod, SoapOperationType soapOperationType, String soapOperationName){
        for(SoapOperation soapOperation : soapPort.getSoapOperations()){
            if(soapOperation.getSoapOperationMethod().equals(soapOperationMethod) && soapOperation.getSoapOperationType().equals(soapOperationType) && soapOperation.getName().equalsIgnoreCase(soapOperationName)){
                return mapper.map(soapOperation, SoapOperationDto.class);
            }
        }
        return null;
    }
}
