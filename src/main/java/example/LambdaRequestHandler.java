/* Amplify Params - DO NOT EDIT
	ENV
	REGION
	STORAGE_MYBUCKET_BUCKETNAME
Amplify Params - DO NOT EDIT */

package example;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;


public class LambdaRequestHandler {
	
	String tableName = System.getenv("STORAGE_DYNAMO9CE41B52_NAME");
	
	
	public void handleRequest(S3Event s3event, Context context) {
		S3EventNotificationRecord record = s3event.getRecords().get(0);
	    String bucketName = record.getS3().getBucket().getName();
	    String fileKey = record.getS3().getObject().getUrlDecodedKey();
//		String fileName = "employee.xlsx"; // Replace with your file name
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
		AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
		DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
		System.out.println("Inside handle Request");
		
		try {
			S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, fileKey));
			InputStream inputStream = s3Object.getObjectContent();
			Workbook workbook = WorkbookFactory.create(inputStream);
			Sheet sheet = workbook.getSheetAt(0); // Assuming data is in first sheet
			Table table = dynamoDB.getTable(tableName);

			List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			int rowCount = 0;
			for (Row row : sheet) {
				if (rowCount++ == 0) {
					continue; // Skip header row
				}

				Map<String, Object> item = new HashMap<String, Object>();
				for (Cell cell : row) {
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						item.put(cell.getColumnIndex() + "", cell.getStringCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						item.put(cell.getColumnIndex() + "", cell.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						item.put(cell.getColumnIndex() + "", cell.getBooleanCellValue());
						break;
					default:
						item.put(cell.getColumnIndex() + "", cell + "");
					}
				}

				items.add(item);
			}
			List<Employee> employees = new ArrayList<Employee>();
			for (Map<String, Object> item : items) {
				String id =(String) item.get("0");
				String name = (String) item.get("1");
				double age =  (double) item.get("2");
				employees.add(new Employee(id,name,age));
				Item dbItem=new Item().withPrimaryKey("id",id)
						.withString("name", name)
						.withNumber("age", age);
				table.putItem(dbItem);
			}
			System.out.println(employees);
			
		} catch (Exception e) {
			System.out.println("Exception "+e);

		}
	}
}

