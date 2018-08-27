import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class main extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "NDgyNjk3OTMwNDMyNzA4NjEx.DmIrIA.fUCJgBEpGODRbZpkTjk3-kmsutA";
        builder.setToken(token);
        builder.addEventListener(new main());
        builder.buildAsync();
    }


    public String[] getItemInfo(String name) {
        String[] itemInfo = new String[3];

        name = name.replace(" ", "%20");
        String searchURL = "https://www.novaragnarok.com/?module=item&action=index&type=&name=" + name;
        String searchHTML = readPage(searchURL);
        if (searchHTML.contains("No items found.")){
            itemInfo[0] ="No items found.";
            return itemInfo;
        }
        //find itemID
        searchHTML = searchHTML.substring(searchHTML.indexOf("<span id=\"card"));
        itemInfo[0] =searchHTML.substring("<span id=\"card".length(), searchHTML.indexOf("\" style="));
        searchHTML = searchHTML.substring(searchHTML.indexOf("<div style=\"font-family:Arial"));
        //find name
        itemInfo[1] = removeBrackets(searchHTML.substring(0, searchHTML.indexOf("</div>")));
        searchHTML = searchHTML.substring(1);
        searchHTML = searchHTML.substring(searchHTML.indexOf("<div style=\"font-family:Arial"));
        searchHTML = searchHTML.substring(0, searchHTML.indexOf("<img src=\""));

        itemInfo[2] = removeBrackets(searchHTML).replace("\t", "");
        return itemInfo;
    }

    public String readPage(String websiteURL){
        String html = "";
        System.setProperty("http.agent", "Chrome");
        try {
            // Create a URL for the desired page
            URL url = new URL(websiteURL);


            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                html += str;
                html += "\n";
                // str is one line of text; readLine() strips the newline character(s)
            }
            in.close();
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        return html;
    }
    /*
    public String readMarket(String itemID)throws IOException {
        System.setProperty("http.agent", "Chrome");
        try {
            System.out.println("1");
            // Create a URL for the desired page
            URL url = new URL("https://www.novaragnarok.com/?module=vending&action=item&id=" + itemID);
            String html = "";

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                html += str;
                html += "\n";
                // str is one line of text; readLine() strips the newline character(s)
            }
            in.close();
            return parseMarket(html);
        } catch (MalformedURLException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
        return "";

    }
*/
    public String parseMarket(String html){

        if (html.indexOf("No items found.") != -1){
            return "No one is selling this item right now.";
        }
        boolean isRefine = false;
        boolean isAd = false;
        boolean isStack = false;

        if (html.indexOf("ea.")!= -1){
            isStack = true;
        }
        else if (html.indexOf("Additional") != -1){
            isAd = true;
            if (html.indexOf("Refine") != -1){
                isRefine = true;
            }
        }
        if (html.indexOf("ea.")!= -1){
            isStack = true;
        }
        String iteminfo;
        String temp = html;
        int amount = 0;
        String x = "";
        while(temp.indexOf("</span>z") != -1) {
            temp = temp.substring(temp.indexOf("</span>z") + 1);
            amount++;
        }
        System.out.println("Amount = " + amount);
        String[] prices = new String[amount];
        String[] refines = new String[amount];
        String[] ads = new String[amount];
        String[] count = new String[amount];
        String[] loc = new String[amount];
        html = html.substring(html.indexOf("<div class=\"item-name\">")+1);
        iteminfo = "<" + html.substring(0, html.indexOf("<img src=\""));
        System.out.println("Iteminfo: "+ iteminfo);
        html = html.substring(html.indexOf("Price"));



        for(int i = 0; i < amount; i++){

            html = html.substring(html.indexOf("td data-order="));
            prices[i] = html.substring(html.indexOf("td data-order=") + "td data-order=".length()+1, html.indexOf("style=")-2);
            html = html.substring(1);
            System.out.println("Price = " + prices[i]);
            if(isRefine) {
                html = html.substring(html.indexOf("td data-order="));
                refines[i] = html.substring(html.indexOf("td data-order=") + "td data-order=".length() + 1, html.indexOf("style=") - 2);
                html = html.substring(1);
                System.out.println("Refine = +" + refines[i]);
            }
            if(isAd) {
                html = html.substring(html.indexOf("td data-order="));
                ads[i] = html.substring(html.indexOf("td data-order=") + "td data-order=".length(), html.indexOf("</td>"));
                ads[i] = ads[i].substring(ads[i].indexOf(">") + 1);
                ads[i] = removeBrackets(ads[i]);
                html = html.substring(1);
                System.out.println("Additional Properties = " + ads[i]);
            }
            if(isStack){
                html = html.substring(html.indexOf("td data-order="));
                count[i] = html.substring(html.indexOf("td data-order=") + "td data-order=".length() + 1, html.indexOf("style=") - 2);
                html = html.substring(1);
                System.out.println("Count = " + count[i]);
            }

            html = html.substring(html.indexOf("data-clipboard-text='"));
            loc[i] = html.substring(html.indexOf("data-clipboard-text='") + "data-clipboard-text='".length()+1, html.indexOf("data-map='")-3);
            html = html.substring(1);
            System.out.println("Location = " + loc[i]);



        }

        return printMarket(iteminfo, prices, refines, ads, count, loc);


    }

    public int[] sort(String[] prices, boolean lowToHigh){
        int[] order = new int[prices.length];
        int price = 1000000000;
        int loc = 0;
        int x;
        for(int i = 0; i < prices.length;i++){
            for (x = 0; x < prices.length;x++){
                if (Integer.parseInt(prices[x])<price){
                    price = Integer.parseInt(prices[x]);
                    loc = x;
                }
            }
            order[i] = x;
        }

        return order;
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public String printMarket(String iteminfo, String[] prices, String[] refines, String[] ads, String[] count, String[] loc){
        boolean hasRefine = false;
        boolean hasAds = false;
        boolean hasCount = false;
        int maxAds = 0;
        String message = "```";
        message += iteminfo;
        message += "\n";
        message += padRight("Price\t", 10);
        if (refines[0] != null){
            hasRefine = true;
            message += padRight("Ref\t", 4);
        }
        if (ads[0] != null){
            for (String ad: ads){
                if (ad.length()>maxAds){
                    maxAds = ad.length();
                }
            }
            hasAds = true;
            message += padRight("Properties\t",maxAds+1);
        }
        if (count[0] != null){
            hasCount = true;
            message += padRight("Count\t", 7);
        }
        message += padRight("Location\t", 10);

        message += "\n";
        for (int i = 0; i < prices.length; i++){
            message += padRight(prices[i] + "\t",10);
            if (hasRefine){
                message += padRight("+" + refines[i] + "\t",4);
            }
            if (hasAds){
                message += padRight(ads[i] + "\t",maxAds+1);
            }
            if (hasCount){
                message += padRight(count[i] +"\t", 7);
            }
            message += padRight(loc[i] + "\t", 10);
            message += "\n";
        }


        message += "```";
        return message;
    }

    public String removeBrackets(String input){
        String noBrackets = "";
        while (input.indexOf("<")!= -1){
            noBrackets+= input.substring(0, input.indexOf("<"));
            input = input.substring(input.indexOf(">")+1);
        }
        noBrackets += input;
        return noBrackets;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        String message = event.getMessage().getContentRaw().toLowerCase();
        if (event.getAuthor().isBot()){
            return;
        }
        System.out.println("Message received from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay());
        //if !ping
        if (message.equals("~ping")){
            event.getChannel().sendMessage("Pong!"
            ).queue();
        }
        //if !kms
        if (message.contains("caw")){
            double chance = Math.random();
            if (chance<.01) {
                event.getChannel().sendMessage("CAW CAW MOTHERFUCKER.").queue();
            }
            else if (chance<.01){
                event.getChannel().sendMessage(event.getAuthor().getAsMention()+" is a nerd").queue();
            }
            else if (chance<.52){
                event.getChannel().sendMessage("CAW.").queue();
            }
            else{
                event.getChannel().sendMessage("SQUAWK.").queue();
            }
        }
        if (message.equals("~kms")){
            event.getChannel().sendMessage(event.getAuthor().getName() + " has died. Use ~f to pay respects.").queue();
        }
        //if !sig
        if (message.startsWith("~sig ")){
            event.getChannel().sendMessage("https://www.novaragnarok.com/ROChargenPHP/newsig/" +
                    event.getMessage().getContentRaw().substring(5).replace(" ", "%20") +
                    "/" + (int)(Math.random()*11) + "/" + (int)(Math.random()* 20)
            ).queue();
        }
        if (message.equals("~f")){
            event.getChannel().sendMessage(":regional_indicator_f:" +
                    event.getAuthor().getName()+ " has paid respects." +
                    ":regional_indicator_f:"
            ).queue();
        }
        if (message.startsWith("~ws")){
            /*
            System.out.println("WS command.");
            try {
                event.getChannel().sendMessage(readMarket(message.substring(4))).queue();
            }
            catch (IOException e){
                System.err.println(e);
            }
            */
        }
        if (message.startsWith("~ii")){
            //0 = id, 1 = name, 2 = description
            //if 0 = "No items found." just print it.
            String[] itemInfo = getItemInfo(message.substring(4));
            if (itemInfo[0].equals("No items found.")){
                event.getChannel().sendMessage(itemInfo[0]).queue();
            }
            else{
                event.getChannel().sendMessage("`"+ itemInfo[0] + ": " + itemInfo[1] +
                        "`\n" + "```" +itemInfo[2]+ "```").queue();
            }
        }
        if (message.startsWith("~cawrates")){
            event.getChannel().sendMessage("```You think you're a master caw-hunter? " +
                    "There's 4 unique caw-messages to find, see if you can find them all!```").queue();
        }
        if (message.equals("\\o\\")){
            event.getChannel().sendMessage("/o/").queue();
        }
        if (message.equals("/o/")){
            event.getChannel().sendMessage("\\o\\").queue();
        }
        if (message.startsWith("~help")){
            if (message.equals("~help")){
                event.getChannel().sendMessage("```Hi! I'm MacawBot! I'm still a work in progress, " +
                        "but please take care of me! Ara can't keep her laptop on and she's kinda lazy, " +
                        "so I'll only be available sometimes. Please use Doggo until I'm finished!\n\n" +
                        "List of Commands:\n" +
                        "   ~sig - generates a sig\n" +
                        "       usage - ~sig [charname]\n" +
                        "   ~kms - outputs a message stating that you died.\n" +
                        "   ~f - outputs a message about paying respect.\n" +
                        "   ~ping - outputs \'Pong!\'\n" +
                        "   ~ii - looks up an item in the NovaRO Database\n" +
                        "       usage - ~ii [itemname/itemID]\n" +
                        "   ~cawrates - CAW." +
                        "```"
                ).queue();
            }

        }
    }
}
