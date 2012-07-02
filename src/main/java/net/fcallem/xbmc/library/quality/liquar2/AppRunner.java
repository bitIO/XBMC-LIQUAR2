package net.fcallem.xbmc.library.quality.liquar2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

/**
 * @see http://www.xuggle.com/
 * @see http://xuggle.googlecode.com/svn/trunk/java/xuggle-xuggler/src/com/xuggle/xuggler/demos/GetContainerInfo.java
 * @see http://code.google.com/p/sqlite-jdbc/
 * @see http://stackoverflow.com/questions/2168472/media-information-extractor-for-java
 */
public class AppRunner {

	private static final Logger log = Logger.getLogger(AppRunner.class.getName());

	private static Connection connection = null;
	private static File outFile 		 = null;
	private static FileWriter fw		 = null;
	private static BufferedWriter bw 	 = null;
	private static Boolean rename		 = false;
	
	/**
	 * 
	 * @param args
	 *            where param 1 is the directory to scan, param 2 the SQLite
	 *            file to be used as reference
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		BasicConfigurator.configure();
		if (args.length < 3) {
			log.warn( "Incorrect number op parameters. I need just 2.");
			log.warn( "Param 1: The directory to scan.");
			log.warn( "Param 2: The SQLite file.");
			log.warn( "Param 3: Path to the output file.");
			log.warn( "Param 4: To rename or not to rename, that is the question. Use rename=<y|n|yes|no>. " +
					"Param is optional" );
			log.warn( "\n\n.... mother fucker.");
			System.exit(1);
		}
		
		if (args.length == 4) {
			String command = args[3].substring(0, args[3].lastIndexOf('='));
			String value = args[3].substring(args[3].lastIndexOf('=') + 1);
			
			if (command.equals("rename")) {
				if (value.startsWith("y")){
					rename = true;
				} else if (value.startsWith("n")){
					rename = false;
				} else {
					log.warn("Rename is disabled. Use rename=<y|n|yes|no>");					
				}
			} else {
				log.warn("Param is not recognized. Check out your finger, maybe they're broken XD");
			}
		}

		File moviesFolder = new File(args[0]);
		if (!moviesFolder.exists()) {
			log.error( "Folder does not exist ¬¬");
			System.exit(1);
		}
		if (!moviesFolder.isDirectory()) {
			log.error( "What? Are you fucking kidding me...arshole?");
			log.error( "You're supposed to give me a fucking directory, not a file. Sucker ¬¬");
			System.exit(2);
		}

		File[] allTheMovies = moviesFolder.listFiles();
		if (allTheMovies == null || allTheMovies.length == 0) {
			log.error( "Are you aware that you have nothing there?");
			log.error( "Go a get some content before using me again ¬¬");
			System.exit(3);
		}

		FileFilter filter = new FileFilter() {

			public boolean accept(File pathname) {
				if (pathname.getName().indexOf('.') == -1) {
					return false;
				}

				String extension = pathname.getName().substring(pathname.getName().lastIndexOf('.') + 1);
				if (extension.equalsIgnoreCase("mkv")) {
					return true;
				}
				if (extension.equalsIgnoreCase("avi")) {
					return true;
				}
				if (extension.equalsIgnoreCase("mpeg")) {
					return true;
				}
				if (extension.equalsIgnoreCase("mp4")) {
					return true;
				}
				if (extension.equalsIgnoreCase("m4v")) {
					return true;
				}
				return false;
			}
		};
		
		// load the sqlite-JDBC driver using the current class loader
	    Class.forName("org.sqlite.JDBC");
	    
		// create a database connection
		connection = DriverManager.getConnection("jdbc:sqlite:" + args[1]);
		
		outFile = new File(args[2]);
		fw= new FileWriter(outFile);
		bw = new BufferedWriter(fw);
		
		bw.write(
				"Filename, Width, Height, FrameRate, Codec, " +
				"Audio #1 lang, Audio #1 codec, Audio #1 channels, " +
				"Audio #2 lang, Audio #2 codec, Audio #2 channels, " +
				"Audio #3 lang, Audio #3 codec, Audio #3 channels, " +
				"Audio #4 lang, Audio #4 codec, Audio #4 channels, " +
				"Audio #5 lang, Audio #5 codec, Audio #5 channels, " +
				"Audio #6 lang, Audio #6 codec, Audio #6 channels, " +
				"\n");
		IContainer container = IContainer.make();
		for (int i = 0; i < allTheMovies.length; i++) {
			if (allTheMovies[i].isDirectory()) {
				File[] moviesInFolder = allTheMovies[i].listFiles(filter);
				if (moviesInFolder.length == 0) {
					log.info( "No movies in folder:" + allTheMovies[i].getName());
					continue;
				}

				if (moviesInFolder.length > 1) {
					log.info( "More than one movie in folder:" + allTheMovies[i].getName());
				}

				if (container.open("file://" + moviesInFolder[0].getAbsolutePath(), IContainer.Type.READ, null) < 0) {
					log.warn("could not open file: " + moviesInFolder[0]);
					continue;
				}
				log.info( "Processing file :" + moviesInFolder[0].getName());
				bw.write(moviesInFolder[0].getName() + ",");
				//showDemoInfo(container, moviesInFolder[0].getName());
				writeFile(container, moviesInFolder[0].getName());
				readXBMCInfo(moviesInFolder[0].getName(), moviesInFolder[0].getParent());
				container.close();
			}
		}
		bw.close();
		fw.close();
		
		log.info("I'm done. Checkout what kinda crap u have ;-)");

	}

	/**
	 * 
	 * @param container
	 * @param filename
	 * @param bw
	 * @throws IOException
	 */
	private static void writeFile(IContainer container, String filename) throws IOException {
		// query how many streams the call to open found and iterate through the streams to print their meta data
		int numStreams = container.getNumStreams();
		ArrayList<String[]> audioList = new ArrayList<String[]>();

		for (int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = container.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();
			
			String codecName = coder.getCodecID().name().substring(coder.getCodecID().name().lastIndexOf('_')+1);
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				bw.write( coder.getWidth() + "," );
				bw.write( coder.getHeight() + "," );
				bw.write( coder.getFrameRate().getDouble() + "," );
				bw.write( codecName + "," );
			}
			else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				audioList.add( new String[]{
						stream.getLanguage(),
						codecName,
						String.valueOf(coder.getChannels())
						});
			}
		}
		for (String[] strings : audioList) {
			bw.write(strings[0] + ", ");
			bw.write(strings[1] + ", ");
			bw.write(strings[2] + ", ");
        }
		bw.write("\n");
	}

	private static void readXBMCInfo(String fileName, String folderName) throws SQLException {
		Statement statement = connection.createStatement();
		ResultSet rs  = statement.executeQuery("SELECT idMovie, c00, c07, c22 FROM movie WHERE c22 LIKE '%" + folderName + "%'");
		if (rs.next()) {
			String cleanName = rs.getString("c00") + " (" + rs.getString("c07") + ")";
			if ( cleanName.equals(folderName.substring(folderName.lastIndexOf('/') + 1 )) ) {
				log.debug("Movie is correctly formated");
			}
			else {
				log.info("Movie is NOT correctly formated");
				if (rename) {
					log.info("Renaming to: " + cleanName);
					File folder = new File(folderName);
					folder.renameTo(new File( folderName.substring(0, folderName.lastIndexOf('/')), cleanName ));
					statement.executeUpdate("UPDATE movie SET c22='" + 
							folderName.substring(0, folderName.lastIndexOf('/') + 1) + cleanName + "/' WHERE idMovie=" + 
							rs.getInt("idMovie"));
				}
			}
		}
		rs.close();
		statement.close();

    }
	
	private static void showDemoInfo(IContainer container, String filename) {
		// query how many streams the call to open found
		int numStreams = container.getNumStreams();
		System.out.printf("file \"%s\": %d stream%s; ", filename, numStreams, numStreams == 1 ? "" : "s");
		System.out.printf("duration (ms): %s; ",
		        container.getDuration() == Global.NO_PTS ? "unknown" : "" + container.getDuration() / 1000);
		System.out.printf("start time (ms): %s; ", container.getStartTime() == Global.NO_PTS ? "unknown" : ""
		        + container.getStartTime() / 1000);
		System.out.printf("file size (bytes): %d; ", container.getFileSize());
		System.out.printf("bit rate: %d; ", container.getBitRate());
		System.out.printf("\n");

		// and iterate through the streams to print their meta data
		for (int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = container.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();

			// and now print out the meta data.
			System.out.printf("stream %d: ", i);
			System.out.printf("type: %s; ", coder.getCodecType());
			System.out.printf("codec: %s; ", coder.getCodecID());
			System.out.printf("duration: %s; ",
			        stream.getDuration() == Global.NO_PTS ? "unknown" : "" + stream.getDuration());
			System.out.printf("start time: %s; ",
			        container.getStartTime() == Global.NO_PTS ? "unknown" : "" + stream.getStartTime());
			System.out.printf("language: %s; ", stream.getLanguage() == null ? "unknown" : stream.getLanguage());
			System.out.printf("timebase: %d/%d; ", stream.getTimeBase().getNumerator(), stream.getTimeBase()
			        .getDenominator());
			System.out.printf("coder tb: %d/%d; ", coder.getTimeBase().getNumerator(), coder.getTimeBase()
			        .getDenominator());

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				System.out.printf("sample rate: %d; ", coder.getSampleRate());
				System.out.printf("channels: %d; ", coder.getChannels());
				System.out.printf("format: %s", coder.getSampleFormat());
			}
			else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				System.out.printf("width: %d; ", coder.getWidth());
				System.out.printf("height: %d; ", coder.getHeight());
				System.out.printf("format: %s; ", coder.getPixelType());
				System.out.printf("frame-rate: %5.2f; ", coder.getFrameRate().getDouble());
			}
			System.out.printf("\n");
		}
	}
}
