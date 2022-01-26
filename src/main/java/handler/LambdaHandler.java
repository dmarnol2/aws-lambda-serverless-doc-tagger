package handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

    public class LambdaHandler implements RequestHandler<Map<String,String>, String> {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        @Override
        public String handleRequest(Map<String,String> event, Context context)
        {
        LambdaLogger logger = context.getLogger();
        String response = "200 OK";
        logger.log("CONTEXT: " + gson.toJson(context));
        logger.log("EVENT: " + gson.toJson(event));


        final String TAG_EXTENSION = ".tag";
        final String DOCUMENT_PATH = event.get("documentPath");
        final String LEXICON_PATH = event.get("lexiconPath");
        logger.log("DOC PATH: " + DOCUMENT_PATH + " and LEX PATH: " + LEXICON_PATH);

        ArrayList<String> documentData = new ArrayList<String>();
        Map<String, String> lexMap = new HashMap<String, String>();

        Scanner scan = new Scanner(System.in);
        System.out.println("Enter path of document to tag: ");
//                String documentPath = scan.nextLine().trim();
        File docToRead = getFile(DOCUMENT_PATH);

        System.out.println("Enter path to lexicon: ");
//                String lexiconPath = scan.nextLine().trim();
        File lexiconToRead = getFile(LEXICON_PATH);

        Scanner docScanner = buildScanner(docToRead);
        Scanner lexScanner = buildScanner(lexiconToRead);

        while (docScanner.hasNextLine()) {
            documentData.add(docScanner.nextLine());
        }
        docScanner.close();

        while (lexScanner.hasNextLine()) {
            String[] temp = lexScanner.nextLine().split("\t");
            lexMap.put(temp[0], temp[1]);
        }
        lexScanner.close();

        File outputDoc = getFile(changeExtension(DOCUMENT_PATH, TAG_EXTENSION));
        buildOutputDoc(documentData, lexMap, outputDoc);
        return response;
    }

    public static void buildOutputDoc(ArrayList<String> documentData, Map lexicon, File outputDoc){
        PrintWriter output = null;
        try{
            output = new PrintWriter(outputDoc.getPath());
            PrintWriter finalOutput = output;
            documentData.forEach(x -> {
                for (String s : x.split("(?=[\\W_])|(?<=[\\W_])")) {
                    String val = (String) lexicon.get(s.toUpperCase());
                    if (val != null) {
                        x = x.replace(s, s.concat("/" + val));
                    }
                }
                finalOutput.println(x);
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(output!=null) {
                output.close();
            }
        }
    }


    public static Scanner buildScanner(File document){
        Scanner docScanner = null;
        try {
            docScanner = new Scanner(document);
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred in building Scanner.");
            e.printStackTrace();
        }
        return docScanner;
    }

    public static String changeExtension(String fileName, String extension) {
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        fileName += extension;
        return fileName;
    }

    public static File getFile(String url){
        File newFile = new File(url);
        try {
            if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile.getPath());
            } else {
                System.out.println("File " + newFile.getName() + " found.");
            }
        } catch (IOException e) {
            System.out.println("There was an error finding or creating file.");
            e.printStackTrace();
        }
        return newFile;
        }
    }
