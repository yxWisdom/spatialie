package edu.nju.ws.spatialie.utils;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.Map;

//enum ChartType{LINE}

public class ChartUtil extends ApplicationFrame {
    public ChartUtil(String applicationTitle) {
        super(applicationTitle);

    }
    public void plotLineChart(String chartTitle, Map<Integer, Integer> data) {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        for (Map.Entry<Integer, Integer> entry: data.entrySet()) {
            dataSet.addValue(entry.getValue(),"document",String.valueOf(entry.getKey()*500));
        }
        JFreeChart lineChart=ChartFactory.createLineChart(chartTitle, "length","count", dataSet);
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1600,1050));
        setContentPane(chartPanel);
    }

}
