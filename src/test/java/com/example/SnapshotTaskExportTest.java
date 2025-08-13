package com.example;

import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Test;

import com.github.database.rider.junit5.api.DBRider;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.ExportDataSet;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.Arrays;

import com.example.task.mybatis.TaskMapper;

@DBRider
class SnapshotTaskExportTest extends BaseTest {

    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(
        Arrays.stream(WELD_CORE_BEANS).toArray(Class[]::new)
    ).activate(RequestScoped.class).build();

    @Inject
    TaskMapper taskMapper;

    @Test
    @DataSet(value = "datasets/tasks.yml", cleanBefore = true)
    @ExportDataSet(format = DataSetFormat.YML, outputName = "tasks-after-export.yml")
    void export_afterInsert() {
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        System.out.println(taskMapper.findAll().get(0).getTitle());
    }
}
