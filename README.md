XBMC-LIQUAR2
=======
**XBMC-LIQUAR2** stands for **XBMC** **LI**brary **QU**ality **R**reporter and **R**enamer
By now, it's just a simple class that reads form a given directory, the SQLite database and extracts de information about the movie. The information is:

* video dimensions (width and height)
* frame rate
* video codec
* audio channels info
	* language
	* codec
	* channels

To use it, you just need to create the runnable jar (into the target directory) by typing

	mvn assembly:single

and the just type
	
	java -jar XBMC-LIQUAR2-{version}-jar-with-dependencies.jar <dir> <sqlite file> <output> [rename]

Where

* `dir` is the directory to scan; by now, we go only one level deep
* `sqlite` is the sqlite file location (see relate links below)
* `output` is the CSV file where the report will be generated
* `rename` if this is specified, this will make the program to rename the folder and update de database


##External references
Links refering to the 

 * <a href="http://wiki.xbmc.org/index.php?title=Userdata">XBMC User data</a>
 * <a href="http://wiki.xbmc.org/index.php?title=HOW-TO:Use_your_computer_to_edit_XBMC_%28SQL%29_database-files">HOW-TO:Use your computer to edit XBMC (SQL) database-files</a>
 *<a href="http://wiki.xbmc.org/index.php?title=XBMC_databases">XBMC Databases</a>
	
Links refering to the code

 * <a href="http://www.xuggle.com/">http://www.xuggle.com/</a>
 * <a href="http://xuggle.googlecode.com/svn/trunk/java/xuggle-xuggler/src/com/xuggle/xuggler/demos/GetContainerInfo.java">http://xuggle.googlecode.com/svn/trunk/java/xuggle-xuggler/src/com/xuggle/xuggler/demos/GetContainerInfo.java</a>
 * <a href="http://code.google.com/p/sqlite-jdbc/">http://code.google.com/p/sqlite-jdbc/</a>
 * <a href="http://stackoverflow.com/questions/2168472/media-information-extractor-for-java">http://stackoverflow.com/questions/2168472/media-information-extractor-for-java</a>
