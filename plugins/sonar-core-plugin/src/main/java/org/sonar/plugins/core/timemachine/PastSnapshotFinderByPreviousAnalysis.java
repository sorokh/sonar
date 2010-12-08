/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.core.timemachine;

import org.sonar.api.BatchExtension;
import org.sonar.api.database.DatabaseSession;
import org.sonar.api.database.model.Snapshot;

import java.text.SimpleDateFormat;
import java.util.List;

public class PastSnapshotFinderByPreviousAnalysis implements BatchExtension {
  public static final String MODE = "previous_analysis";

  private Snapshot projectSnapshot; // TODO replace by PersistenceManager
  private DatabaseSession session;

  public PastSnapshotFinderByPreviousAnalysis(Snapshot projectSnapshot, DatabaseSession session) {
    this.projectSnapshot = projectSnapshot;
    this.session = session;
  }

  PastSnapshot findByPreviousAnalysis() {
    String hql = "from " + Snapshot.class.getSimpleName() + " where createdAt<:date AND resourceId=:resourceId AND status=:status and last=true order by createdAt desc";
    List<Snapshot> snapshots = session.createQuery(hql)
        .setParameter("date", projectSnapshot.getCreatedAt())
        .setParameter("resourceId", projectSnapshot.getResourceId())
        .setParameter("status", Snapshot.STATUS_PROCESSED)
        .setMaxResults(1)
        .getResultList();

    if (snapshots.isEmpty()) {
      return null;
    }
    Snapshot snapshot = snapshots.get(0);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    return new PastSnapshot(MODE, snapshot.getCreatedAt(), snapshot).setModeParameter(format.format(snapshot.getCreatedAt()));
  }

}