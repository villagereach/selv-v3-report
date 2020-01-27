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

package org.openlmis.selv.report.domain;

import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "jasper_template_parameter_dependencies")
@NoArgsConstructor
public class JasperTemplateParameterDependency extends BaseEntity {
  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "parameterId", nullable = false)
  @Getter
  @Setter
  private JasperTemplateParameter parameter;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, nullable = false)
  @Getter
  @Setter
  private String dependency;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, nullable = false)
  @Getter
  @Setter
  private String property;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, nullable = false)
  @Getter
  @Setter
  private String placeholder;

  /**
   * Creates a new instance of jasper template dependency.
   *
   * @param dependency dependent parameter.
   * @param property dependent parameter's property to extract.
   * @param placeholder URI placeholder for parameter's property.
   */
  public JasperTemplateParameterDependency(
      String dependency, String property, String placeholder) {
    this.dependency = dependency;
    this.property = property;
    this.placeholder = placeholder;
  }

  /**
   * Create new instance of JasperTemplateParameter based on given
   * {@link JasperTemplateParameter.Importer}.
   *
   * @param importer instance of {@link JasperTemplateParameter.Importer}
   * @return instance of JasperTemplateParameter.
   */
  public static JasperTemplateParameterDependency newInstance(
      JasperTemplateParameterDependency.Importer importer) {
    JasperTemplateParameterDependency dependency = new JasperTemplateParameterDependency();

    dependency.setId(importer.getId());
    dependency.setDependency(importer.getDependency());
    dependency.setProperty(importer.getProperty());
    dependency.setPlaceholder(importer.getPlaceholder());

    return dependency;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(JasperTemplateParameterDependency.Exporter exporter) {
    exporter.setId(id);
    exporter.setDependency(dependency);
    exporter.setProperty(property);
    exporter.setPlaceholder(placeholder);
  }

  public interface Exporter {
    void setId(java.util.UUID id);

    void setDependency(String dependency);

    void setProperty(String property);

    void setPlaceholder(String placeholder);
  }

  public interface Importer {
    UUID getId();

    String getDependency();

    String getProperty();

    String getPlaceholder();
  }
}
