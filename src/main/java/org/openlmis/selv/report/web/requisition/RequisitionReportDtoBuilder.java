/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.selv.report.web.requisition;

import java.util.List;
import java.util.Optional;
import org.openlmis.selv.report.dto.external.referencedata.UserDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionLineItemDto;
import org.openlmis.selv.report.dto.external.requisition.RequisitionStatusDto;
import org.openlmis.selv.report.dto.external.requisition.StatusChangeDto;
import org.openlmis.selv.report.dto.requisition.RequisitionReportDto;
import org.openlmis.selv.report.i18n.MessageKeys;
import org.openlmis.selv.report.i18n.MessageService;
import org.openlmis.selv.report.service.referencedata.UserReferenceDataService;
import org.openlmis.selv.report.utils.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequisitionReportDtoBuilder {

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private MessageService messageService;
  
  /**
   * Create a {@link RequisitionReportDto} based on a given {@link RequisitionDto}.
   *
   * @param requisition a single {@link RequisitionDto} to be converted to report dto.
   * @return a single {@link RequisitionReportDto}
   */
  public RequisitionReportDto build(RequisitionDto requisition) {
    List<RequisitionLineItemDto> fullSupply =
        requisition.getNonSkippedFullSupplyRequisitionLineItems();
    List<RequisitionLineItemDto> nonFullSupply =
        requisition.getNonSkippedNonFullSupplyRequisitionLineItems();

    RequisitionReportDto reportDto = new RequisitionReportDto();
    reportDto.setRequisition(requisition);
    reportDto.setFullSupply(fullSupply);
    reportDto.setNonFullSupply(nonFullSupply);
    reportDto.setFullSupplyTotalCost(requisition.getFullSupplyTotalCost());
    reportDto.setNonFullSupplyTotalCost(requisition.getNonFullSupplyTotalCost());
    reportDto.setTotalCost(requisition.getTotalCost());

    List<StatusChangeDto> statusChanges = requisition.getStatusHistory();
    if (statusChanges != null) {
      Optional<StatusChangeDto> initiatedEntry = statusChanges.stream()
          .filter(statusChange -> statusChange.getStatus() == RequisitionStatusDto.INITIATED)
          .findFirst();
      if (initiatedEntry.isPresent()) {
        reportDto.setInitiatedBy(getUser(initiatedEntry.get()));
        reportDto.setInitiatedDate(initiatedEntry.get().getCreatedDate());
      }

      Optional<StatusChangeDto> submittedEntry = statusChanges.stream()
          .filter(statusChange -> statusChange.getStatus() == RequisitionStatusDto.SUBMITTED)
          .findFirst();
      if (submittedEntry.isPresent()) {
        reportDto.setSubmittedBy(getUser(submittedEntry.get()));
        reportDto.setSubmittedDate(submittedEntry.get().getCreatedDate());
      }

      Optional<StatusChangeDto> authorizedEntry = statusChanges.stream()
          .filter(statusChange -> statusChange.getStatus() == RequisitionStatusDto.AUTHORIZED)
          .findFirst();
      if (authorizedEntry.isPresent()) {
        reportDto.setAuthorizedBy(getUser(authorizedEntry.get()));
        reportDto.setAuthorizedDate(authorizedEntry.get().getCreatedDate());
      }
    }

    return reportDto;
  }

  private UserDto getUser(StatusChangeDto statusChange) {
    UserDto user;

    if (statusChange.getAuthorId() == null) {
      /*
       * This will happen for Javers entries that were generated by the system
       * in the AuditLogInitializer class at startup. This concerns primarily
       * pre-made requisitions, either from demo-data or inserted to the database
       * through other means. This is a solution to handle those entries and display
       * the user as 'SYSTEM' in the report. If one wishes more detailed information for
       * their data, they will have to consider correctly populating Javers tables in
       * their ETL process.
      */
      Message.LocalizedMessage localizedMessage = messageService.localize(
          new Message(MessageKeys.REQUISITION_STATUS_CHANGE_USER_SYSTEM));
      String system = localizedMessage.asMessage();

      user = new UserDto();
      user.setUsername(system);
      user.setFirstName(system);
    } else {
      user = userReferenceDataService.findOne(statusChange.getAuthorId());
    }

    return user;
  }
}