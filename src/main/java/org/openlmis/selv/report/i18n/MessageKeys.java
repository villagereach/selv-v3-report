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

package org.openlmis.selv.report.i18n;

import java.util.Arrays;

public class MessageKeys {
  private static final String DELIMITER = ".";

  // General
  protected static final String SERVICE = "report";
  protected static final String SERVICE_ERROR = join(SERVICE, "error");

  protected static final String NOT_FOUND = "notFound";

  protected static final String REQUISITION_ERROR = "requisition.error";
  public static final String ERROR_REQUISITION_NOT_FOUND = REQUISITION_ERROR
      + ".requisitionNotFound";
  public static final String REQUISITION_STATUS_CHANGE_USER_SYSTEM =
      REQUISITION_ERROR + ".statusChange.user.system";
  public static final String REQUISITION_ERROR_IO = REQUISITION_ERROR + ".io";
  public static final String REQUISITION_ERROR_JASPER_FILE_FORMAT =
      REQUISITION_ERROR + ".jasper.file.format";

  protected static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }

  protected MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
