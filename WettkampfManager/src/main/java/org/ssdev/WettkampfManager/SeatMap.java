package org.ssdev.WettkampfManager;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.poi.hpsf.ReadingNotSupportedException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

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
	
	protected static String getStringValue(Cell cell) {
		cell.setCellType(1);
		return cell.getStringCellValue();
	}
	
	public int doExcelImport(HSSFWorkbook workbook) {
		myLock.writeLock().lock();
		int cnt = 0;
		try {
			HSSFSheet sheet = workbook.getSheetAt(0);
			
			// Memorize which seats are taken in case we need to auto-assign seats
			HashMap<String, HashSet<String>> takenSeatings = new HashMap<String, HashSet<String>>();
			HashSet<String> seatedNames = new HashSet<>();
			
			// Remove header row
			sheet.removeRow(sheet.getRow(0));
			
			for (Row row : sheet) {
				String name = getStringValue(row.getCell(0));
				String table = getStringValue(row.getCell(1));
				String seating = getStringValue(row.getCell(2));
				this.changeName(table, seating, name);
				cnt++;
				
				if (!takenSeatings.containsKey(table)) {
					takenSeatings.put(table, new HashSet<String>());
				}
				takenSeatings.get(table).add(seating);
				seatedNames.add(name);
			}

			if (workbook.getNumberOfSheets() == 3) {
				HSSFSheet contestantSheet = workbook.getSheetAt(1);
				HSSFSheet seatingsSheet = workbook.getSheetAt(2);
				
				contestantSheet.removeRow(contestantSheet.getRow(0));
				seatingsSheet.removeRow(seatingsSheet.getRow(0));
				
				List<Integer> availableSeatsIdx = new ArrayList<Integer>();
				List<String> availableContestants = new ArrayList<String>();
				
				int rowIdx = 1;
				for (Row row : seatingsSheet) {
					String table = getStringValue(row.getCell(0));
					String seating = getStringValue(row.getCell(1));
					
					if (!takenSeatings.containsKey(table) || !takenSeatings.get(table).contains(seating)) {
						availableSeatsIdx.add(rowIdx);
					}
					
					rowIdx++;
				}
				
				for (Row row : contestantSheet) {
					String name = getStringValue(row.getCell(0));
					if (!seatedNames.contains(name)) {
						availableContestants.add(name);
					}
				}
				
				if (availableContestants.size() == availableSeatsIdx.size()) {
				    SecureRandom random = new SecureRandom();
					Collections.shuffle(availableSeatsIdx, random);		
					
					int listIdx = 0;
					for (Integer seatIdx : availableSeatsIdx) {
						System.err.println(seatIdx);
						String table = getStringValue(seatingsSheet.getRow(seatIdx).getCell(0));
						String seating = getStringValue(seatingsSheet.getRow(seatIdx).getCell(1));
						this.changeName(table, seating, availableContestants.get(listIdx));
						listIdx++;
						cnt++;
					}
				} else {
					System.err.println("Mismatch between constestants and seats");
				}
								
				// TODO: Implement display method
			}
			
			return cnt;
		} finally {
			myLock.writeLock().unlock();
		}
	}
}
