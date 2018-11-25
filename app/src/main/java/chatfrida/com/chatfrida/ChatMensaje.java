package chatfrida.com.chatfrida;

public class ChatMensaje {

    private String msgText;
    private String msgUser;

    public ChatMensaje(String msgText, String msgUser){
        this.msgText = msgText;
        this.msgUser = msgUser;
    }

    public ChatMensaje(){
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getMsgUser() {

        return msgUser;
    }

    public void setMsgUser(String msgUser) {
        this.msgUser = msgUser;
    }
}
