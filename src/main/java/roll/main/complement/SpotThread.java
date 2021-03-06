package roll.main.complement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import roll.automata.NBA;
import roll.automata.operations.nba.inclusion.NBAInclusionCheckTool;
import roll.main.Options;

public class SpotThread extends Thread {
	Boolean result = false;
	
	NBA spotBFC;
	NBA spotB;
	
	Process process;
	
	boolean flag;
	
	Options options;
	
	public SpotThread(NBA BFC, NBA B, Options options) {
		this.spotBFC = BFC;
		this.spotB = B;
		this.options = options;
		this.flag = true;
	}
	
	public Boolean getResult() {
		return result;
	}
	
	@Override
	public void run() {
		String command = "autfilt --included-in=";
		File fileA = new File("/tmp/A.hoa");
        File fileB = new File("/tmp/B.hoa");
        
        try {
        	NBAInclusionCheckTool.outputHOAStream(spotB, new PrintStream(new FileOutputStream(fileA)));
        	NBAInclusionCheckTool.outputHOAStream(spotBFC, new PrintStream(new FileOutputStream(fileB)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Runtime rt = Runtime.getRuntime();
        // check whether it is included in A.hoa
        command = command + fileA.getAbsolutePath() + " " + fileB.getAbsolutePath();
        options.log.println(command);
        process = null;
        try {
        	process = rt.exec(command);
        	while(flag && process.isAlive()) {
        		// do nothing here
        	}
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if(flag) {
        	final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            try {
                while (flag && (line = reader.readLine()) != null ) {
                    if (line.contains("HOA")) {
                        result = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void interrupt() {
		flag = false;
		if(process != null) {
			process.destroyForcibly();
		}
		super.interrupt();
		this.stop();
	}
}
