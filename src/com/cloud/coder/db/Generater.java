package com.cloud.coder.db;

import com.cloud.actions.CodeMakerAction;
import com.cloud.util.DBTool;
import com.cloud.util.DataConversion;
import com.cloud.util.StringUtil;
import com.cloud.util.UpperFirstCharacter;
import com.intellij.openapi.ui.Messages;
import freemarker.core.ParseException;
import freemarker.template.*;

import javax.swing.*;
import java.io.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 生产主类
 * @author ethan
 *
 */
public class Generater {
//	private Log log = LogFactory.getLog(this.getClass());
//	private static ResourceBundle bundle = ResourceBundle.getBundle("config");

	public static String baseClassPath = "";
	public static String modelGenerateType="points";
	public static String template = "";
	public static String codeDirectory = "";

	private JTextArea console;

	private Pattern p = Pattern.compile("public\\s+(class|interface)\\s+\\w+");

	public void generateAll(List<TableVar> tables, JProgressBar progressBar, JTextArea console) {
		this.console = console;
		if(baseClassPath!=null && !"".equals(baseClassPath.trim())){
			final int[] size = {0};
			ExecutorService executorService = Executors.newFixedThreadPool(tables.size());
			for(TableVar table : tables){
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						generateSimple(baseClassPath,table.getObjectName(), table);
						System.out.println("============================成功生成数据表相关类，数据表为："+table.getTableName()+"=============================");
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								console.append(">>>>>>>>成功生成数据表相关类，数据表为："+table.getTableName()+"\n");
							}
						});
						synchronized (this){
							size[0]++;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									progressBar.setString("进度提示： （"+size[0] + "/" + tables.size() + ")");
									progressBar.setValue(size[0]);
									if(size[0]==tables.size()){
										progressBar.setString("任务完成（"+size[0] + "/" + tables.size() + ")");
									}
								}
							});
						}
					}
				});

//				while (true){
//					try {
//						Thread.sleep(500);
//						System.out.println("========================================================================================================="+size[0]);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}finally {
//						if(size[0]==0){
//							break;
//						}
//					}
//				}
			}
		}else{
			Messages.showErrorDialog("请在 < config.properties > 配置文件中配置“baseClassPath”参数值！", "错误信息：");
		}
	}
	public void generateSimple(String baseClassPath, String modelPath, TableVar table) {
		if(modelGenerateType!=null && modelGenerateType!="" && modelGenerateType!="default"){
			if(modelGenerateType.equals("underline")){
				//将数据库表名称格式：xx_xx、 xx_Xx、  Xx_xx、 xxXx  统一转换为：xx_xx
				modelPath = StringUtil.string2underline(modelPath);
			}else if(modelGenerateType.equals("points")){
				//将数据库表名称格式：xx_xx、 xx_Xx、  Xx_xx、 xxXx  统一转换为：xx.xx
				modelPath = StringUtil.string2points(modelPath);
			}else if(modelGenerateType.equals("hump")){
				//将数据库表名称格式：xx_xx、 xx_Xx、  Xx_xx、 xxXx  统一转换为：xxXx
				modelPath = StringUtil.string2hump(modelPath);
			}
		}
		
		modelPath = baseClassPath + "." + modelPath;
		try {
			List<ColumnMeta> cmList = DBTool.getAllColumnInfo(table.getTableName());
			
			List<ColumnVar> variables = DataConversion.MataToVarForColumn(cmList);
			
			String entityClassPath = this.generateModel(modelPath,table, variables, new String[]{});
			String daoClassPath = this.generateDao(modelPath,table, variables,new String[]{entityClassPath});
			String serviceInterfaceClassPath = this.generateServiceInterface(modelPath,table, variables, new String[]{entityClassPath});
			String serviceImplClassPath = this.generateService(modelPath,table, variables, new String[]{entityClassPath,serviceInterfaceClassPath});
			String controllerClassPath = this.generateController(modelPath,table, variables, new String[]{entityClassPath});
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	public String generateModel(String modelPath, TableVar table, List<ColumnVar> variables, String[] imports) {
		String templatePath = "Model.tlp";
		String classPath = "entity";
		String filePath = this.generate(templatePath, modelPath, classPath, table, variables, imports);
		System.out.println("生成entity成功："+filePath);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				console.append("生成entity成功："+filePath+"\n");
			}
		});
		return filePath;
	}
	
	public String generateController(String modelPath, TableVar table, List<ColumnVar> variables, String[] imports) {
		String templatePath = "Controller.tlp";
		String classPath = "controller";
		String filePath = this.generate(templatePath, modelPath, classPath, table, variables, imports);
		System.out.println("生成Controller成功："+filePath);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				console.append("生成Controller成功："+filePath+"\n");
			}
		});
		return filePath;
	}


	public String generateServiceInterface(String modelPath, TableVar table, List<ColumnVar> variables, String[] imports) {
		String templatePath = "Service.tlp";
		String classPath = "service";
		String filePath = this.generate(templatePath, modelPath, classPath, table, variables, imports);
		System.out.println("生成ServiceInterface成功："+filePath);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				console.append("生成ServiceInterface成功："+filePath+"\n");
			}
		});
		return filePath;
	}
	
	public String generateService(String modelPath, TableVar table, List<ColumnVar> variables, String[] imports) {
		String templatePath = "ServiceImple.tlp";
		String classPath = "service.impl";
		String filePath = this.generate(templatePath, modelPath, classPath, table, variables, imports);
		System.out.println("生成Service成功："+filePath);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				console.append("生成Service成功："+filePath+"\n");
			}
		});
		return filePath;
	}

	
	public String generateDao(String modelPath, TableVar table, List<ColumnVar> variables, String[] imports) {
		String templatePath = "IMapper.tlp";
		String classPath = "mapper";
		String filePath = this.generate(templatePath, modelPath, classPath, table, variables, imports);
		System.out.println("生成Dao成功："+filePath);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				console.append("生成Dao成功："+filePath+"\n");
			}
		});
		return filePath;
	}
	
	/**
	 * 
	 * @param templatePath
	 * @param modelPath
	 * @param classPath
	 * @param table
	 * @param variables
	 * @param imports
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String generate(String templatePath, String modelPath, String classPath, TableVar table, List<ColumnVar> variables, String[] imports){
		
		classPath = modelPath+"."+classPath;
		try {
			Configuration cfg = new Configuration();
			cfg.setClassForTemplateLoading(CodeMakerAction.class, "/template/"+template+"/"); // 指定模板所在的classpath目录
			cfg.setDefaultEncoding("UTF-8");
			cfg.setSharedVariable("upperFC", new UpperFirstCharacter()); // 添加一个"宏"共享变量用来将属性名首字母大写
			
			Template t = cfg.getTemplate(templatePath);
			
			String className = StringUtil.initialStrToUpper(table.getTableName().trim());
			className = StringUtil.convertUnderLine(className);
			
			String filePath = StringUtil.convertPoint(classPath);
			filePath = codeDirectory.endsWith("\\")?codeDirectory:(codeDirectory+"\\") + filePath + "\\";
			File file =  new File(filePath);
			if(!file.exists() || !file.isDirectory()){
				file.mkdirs();
			}
			
			String primaryKeyType = "";
			for(ColumnVar var : variables){
				if(var.isPrimaryKey()){
					primaryKeyType = var.getTypeName();
				}
			}
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("table", table);
			data.put("properties", variables);
			data.put("primaryKeyType", primaryKeyType);
			data.put("package", classPath); //包名
			data.put("modelPath", modelPath);
			data.put("imports", imports);
			
			/*从模板中获取类名称~~~~~~开始 */
	        StringWriter stringWriter=new StringWriter(); 
	        t.process(data, stringWriter);//模板编译后的结果放入StringWriter中
	        
	        Matcher m=p.matcher(stringWriter.toString());
	        if(m.find()){
	        	String[] tmp = m.group().split(" ");
	        	className = tmp[tmp.length-1];
	        }else{
	        	JOptionPane.showMessageDialog(null, "模板出现问题，找不到类名称！\n请检查模板："+templatePath, "错误信息：", JOptionPane.ERROR_MESSAGE);
	        }
	        /*从模板中获取类名称~~~~~~结束 */
	        
	        filePath = filePath + className;
			File file1 = new File(filePath + ".java");
			if(!file1.exists()){
				FileOutputStream fos = new FileOutputStream(file1); // java文件的生成目录
				t.process(data, new OutputStreamWriter(fos, "utf-8"));//模板编译后的结果写入文件流
				fos.flush();
				fos.close();
			}

			classPath = classPath + "." + className;
		} catch (TemplateNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedTemplateNameException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return classPath;
	}

}
