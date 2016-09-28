package org.ssdev.WettkampfManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javafx.beans.property.StringProperty;

public class SeatMap {
	private final Map<String, Map<String, String>> mySeatMap = new HashMap<String, Map<String, String>>();
	private final ReadWriteLock myLock = new ReentrantReadWriteLock();
	
	private SeatMap() {}
	private static SeatMap myInstance = null;
	
	public static SeatMap getInstance() {
		if (myInstance == null) {
			myInstance = new SeatMap();
		}
		return myInstance;
	}

	public void changeName(String table, String seating, String name) {
		myLock.writeLock().lock();
		try {
			Map<String, String> tableMap = mySeatMap.get(table);
			
			if (tableMap == null) {
				tableMap = new HashMap<String, String>();
				mySeatMap.put(table, tableMap);
			}
			
			tableMap.put(seating, name);
		} finally {
			myLock.writeLock().unlock();
		}
	}
	
	public String getName(String table, String seating) {
		myLock.readLock().lock();
		try {
			Map<String, String> tableMap = mySeatMap.get(table);
			
			if (tableMap == null) {
				return "Unknown";
			}
			
			String name = tableMap.get(seating);
			
			if (name == null) {
				return "Unknown";
			}
			
			return name;
		} finally {
			myLock.readLock().unlock();
		}
	}

	public HSSFWorkbook getExcelExport() {
		myLock.readLock().lock();
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet("Sample sheet");
			 
			Map<String, Object[]> data = new HashMap<String, Object[]>();
			data.put("1", new Object[] {"Name", "Tisch", "Platz"});
			
			Integer cnt = 2;
			
			for (Entry<String, Map<String, String>> tableEntry : mySeatMap.entrySet()) {
				String table = tableEntry.getKey();
				Map<String, String> tableMap = tableEntry.getValue();
				
				for (Entry<String, String> seatEntry : tableMap.entrySet()) {
					data.put(cnt.toString(), new Object[] { seatEntry.getValue(), table, seatEntry.getKey() });
					cnt++;
				}
			}
			
			Set<String> keyset = data.keySet();
			int rownum = 0;
			for (String key : keyset) {
			    Row row = sheet.createRow(rownum++);
			    Object [] objArr = data.get(key);
			    int cellnum = 0;
			    for (Object obj : objArr) {
			        Cell cell = row.createCell(cellnum++);
			        if(obj instanceof String)
			          cell.setCellValue((String)obj);
			    }
			}
			
			return workbook;
		} finally {
			myLock.readLock().unlock();
		}
	}
	
	public int doExcelImport(HSSFWorkbook workbook) {
		myLock.writeLock().lock();
		int cnt = 0;
		try {
			HSSFSheet sheet = workbook.getSheetAt(0);
			for (Row row : sheet) {		
				String name = row.getCell(0).getStringCellValue();
				String table = row.getCell(1).getStringCellValue();
				String seating = row.getCell(2).getStringCellValue();
				this.changeName(table, seating, name);
				cnt++;
			}
			return cnt;
		} finally {
			myLock.writeLock().unlock();
		}
	}
}
