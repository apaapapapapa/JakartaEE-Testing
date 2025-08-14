package com.example;

import org.junit.jupiter.api.Test;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.DataSetFormat;
import com.github.database.rider.core.api.exporter.ExportDataSet;

import jakarta.inject.Inject;

import com.example.task.mybatis.TaskMapper;

class SnapshotTaskExportTest extends BaseTest {

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
