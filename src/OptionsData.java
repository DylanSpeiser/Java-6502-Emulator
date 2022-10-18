import java.io.Serializable;
import java.awt.Color;

public class OptionsData implements Serializable {
    //Default Options
    String optionsFileString = "";
    String defaultSaveDirectory = System.getProperty("os.name").toUpperCase().contains("WIN") ?
        System.getenv("AppData")+"/JavaEaterEmulator/" :
        System.getProperty("user.home") + "/Library/Application Support/JavaEaterEmulator/";
    String defaultFileChooserDirectory = System.getProperty("user.home") + System.getProperty("file.separator")+ "Documents";
    int VIA_Address = 0x6000;
    int GPUWidth = GPU.width;
    int GPUHeight = GPU.height;
    int GPUCols = GPU.n_cols;
    int GPURows = GPU.n_rows;
    int GPUBufferBegin = 0x4000;
    int GPUMode = 0;                            //GPU Modes (planned): 0=Character, 1=Bitmap Mono, 2=Character w/ Color, 3=Bitmap w/ Color
    Color bgColor = Color.blue;
	Color fgColor = Color.white;
    
    @Override
    public String toString() {
        return "default file chooser directory: "+defaultFileChooserDirectory+"\n"+
        "Default Save Directory: "+defaultSaveDirectory+"\n"+
        "GPU Resolution: ("+GPUWidth+","+GPUHeight+")\n"+
        "VIA Address: 0x"+Integer.toHexString(VIA_Address)+"\n"+
        "GPU Buffer Address: 0x"+Integer.toHexString(GPUBufferBegin)+"\n"+
        "GPU Character Dimentsions: "+GPUCols+"x"+GPURows+"\n"+
        "GPU Mode: "+GPUMode+"\n"+
        "Background Color: "+bgColor.toString()+"\n"+
        "Foreground Color: "+fgColor.toString()+"\n";
    }
}
