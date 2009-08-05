package com.docdoku.server.http;

import com.docdoku.core.util.FileIO;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 *
 * @author 
 */
public class PlayerProperties {

    private final static String[] SUPPORTED_FORMATS={"flv","mp4","mpg","mp3","aac","mov"};

    private String playerFile;

    private String playerName="";
    private String width, height;
    private String movieValue;


    public PlayerProperties(String dateUrl, String playerPath) throws UnsupportedEncodingException {
     String extension=FileIO.getExtension(dateUrl);

     this.movieValue=FileIO.encodeURL(dateUrl);
    if(Arrays.asList(SUPPORTED_FORMATS).contains(extension)){
         this.playerName="flashplayer";
         this.playerFile=playerPath;
     }

//     if("wma".equals(extension)){
//         this.playerName="wmplayer";
//         this.playerFile=null;
//         this.width="425";
//         this.height="50";
   //  }else
         if("mp3".equals(extension)){
         this.width="425";
         this.height="20";
         }else{
         this.width="425";
         this.height="300";
    }

    }

    public String getMovieValue() {
        return this.movieValue;
    }

     public PlayerProperties(String dateUrl) throws UnsupportedEncodingException {
     String extension=FileIO.getExtension(dateUrl);
     

     this.movieValue=FileIO.encodeURL(dateUrl);
     
     if(Arrays.asList(SUPPORTED_FORMATS).contains(extension)){
         this.playerName="flashplayer";
         this.playerFile=null;
     }

     if("wma".equals(extension)){
         this.playerName="wmplayer";
         this.playerFile=null;
         this.width="425";
         this.height="50";
     }else if("mp3".equals(extension)){
         this.width="425";
         this.height="20";
    }else{
         this.width="425";
         this.height="300";
    }

    }

    public String getWidth() {
        String widthReturn=this.width+"px";
        return widthReturn;
    }

    public String getHeight() {
       String heightReturn=this.height+"px";
       return heightReturn;
    }


    public String getPlayerFile() {
        String playerFileReturn="\""+this.playerFile+"\"";
       return playerFileReturn;
        
    }




    public String getHtmlCode() throws UnsupportedEncodingException{
    String objectBalise="";
//    if ("wmplayer".equals(playerName)){
//          objectBalise="<object type=\"application/x-oleobject\" width="+getWidth()+" height="+getHeight()+" codebase=\"http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=6,4,7,1112\">" +
//          "<param name=\"filename\" value=\""+movieValue+"\" />"+
//          "<param name=\"Showcontrols\" value=\"True\">" +
//          "<param name=\"autoStart\" value=\"false\">" +
//          "<embed type=\"application/x-mplayer2\" Showcontrols=\"true\" autoStart=\"false\" src=\""+movieValue+"\" width="+getWidth()+" height="+getHeight()+"></embed>" +
//          "</object>";
//    }

      if ("flashplayer".equals(playerName)){
          objectBalise="<object type=\"application/x-shockwave-flash\" data="+getPlayerFile()+" width="+getWidth()+" height="+getHeight()+">"
                +"<param name=\"movie\" value="+getPlayerFile()+" />"+
                "<param name=\"allowscriptaccess\" value=\"always\" />"+
                "<param name=\"allowfullscreen\" value=\"true\" />"+
                "<param name=\"flashvars\" value=\"file="+this.movieValue+"\"/>"+
                "<embed type=\"application/x-shockwave-flash\" flashvars=\"file="+this.movieValue+"\" allowfullscreen=\"true\" allowscriptaccess=\"true\" src="+getPlayerFile()+" width="+getWidth()+" height="+getHeight()+"></embed>"
               +"</object>";
        }
      return objectBalise;
    }

}
