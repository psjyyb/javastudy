package chat;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServerMessageWriter extends Thread{
    private Socket socket;
    private String id;
    private Map<String , PrintWriter> chatClients;
    private PrintWriter out=null;
    private BufferedReader in=null;
    private PrintWriter pw ;
    private Map<Integer, List<String>> room = new HashMap<>();
    private int count;
    private int nowRoom;
    int idx = 0;
    private Set<Integer> possibleNum = new HashSet<>();
    public ServerMessageWriter(Socket socket , Map<String, PrintWriter> chatClients, Map<Integer, List<String>> room , int count ,PrintWriter pw ) {
        this.socket = socket;
        this.chatClients = chatClients;
        this.room = room;
        this.count = count;
        this.pw =pw;
        try{
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Boolean check = true;
                Set<String> nicknames = chatClients.keySet();
            while (true) {
                check= true;
                id=in.readLine();
                if(chatClients.containsKey(id)){

                    out.println("중복된 닉네임입니다 다시입력하세요");
                    check=false;
                }
                if(check){
                    broadcast(id+"닉네임의 사용자가 연결했습니다");
                    break;
                }
            }
            out.println("방 목록 보기 : /list\n" +
                    "방 생성 : /create\n" +
                    "방 입장 : /join [방번호]\n" +
                    "방 나가기 : /exit\n" +
                    "접속종료 : /bye\n"+
                    "현재 접속중인 유저 목록 : /users\n"+
                    "현재 참가한 채팅방의 유저목록 : /roomusers\n"+
                    "귓속말 : /whisper 유저이름 보낼말");
            System.out.println(socket.getInetAddress()+" ip가 접속했습니다.");

            synchronized (chatClients){
                chatClients.put(this.id,out);
            }
            synchronized (room){
                for(int i=0;i<this.count;i++){
                    room.put(i,room.get(i));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        String msg = null;
        try{

            while((msg = in.readLine())!=null){
                if("/bye".equals(msg)){//프로그램종료
                    break;
                }else if("/list".equals(msg)){//방목록
                    Set<Integer> keys = room.keySet();
                    for(int key : keys){
                        String str = String.valueOf(key);
                        broadcast(str+"번 방");
                    }
                }else if("/create".equals(msg)){//방생성
                    int cnt=0;

                    if(!possibleNum.isEmpty()){
                        Iterator<Integer> iterator = possibleNum.iterator();
                        cnt = iterator.next();
                        iterator.remove();
                    }else{
                        cnt = room.size()+1;
                    }

                    room.put(cnt,new ArrayList<>());
                    sendMsg(cnt+"번 방이 생성되었습니다.");
                }else if(msg.startsWith("/join")){//방참가
                    int firstSpaceIndex = msg.indexOf(" ");
                    //if(firstSpaceIndex == -1) return;//첫번째 공백이 없으면 리턴
                    String message = msg.substring(firstSpaceIndex+1);

                    try{
                        idx = Integer.parseInt(message.trim());

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    List<String> joinUser = room.get(idx);
                    joinUser.add(id);
                    room.put(idx,joinUser);
                    broadcast(idx,id+"님이"+ idx+"번 방에 참가하셨습니다.");
//                    pw = new PrintWriter("roomTalk"+idx+".txt");
                }else if("/exit".equals(msg)){//방나가기
                    List<String> exitUser = room.get(idx);
                    exitUser.remove(id);
                    room.put(idx,exitUser);
                    if(room.get(idx).isEmpty()){
                        room.remove(idx);
                        possibleNum.add(idx);
                        sendMsg(idx+"번 방을 제거합니다.");
                    }
                    nowRoom = idx;
                    sendMsg(idx+"번 방에서 나갔습니다.");
                    broadcast(idx,id+"님이 나갔습니다.");
                }else if("/users".equals(msg)){
                    Set<String>users = chatClients.keySet();
                    sendMsg("현재 접속 중인 유저");
                    sendMsg("===============");
                    for(String user : users){
                        sendMsg(user);
                    }
                }else if("/roomusers".equals(msg)){
                    sendMsg(String.valueOf(idx)+"번 방에 있는 유저 목록입니다.");
                    sendMsg("==============");
                    List<String> roomUser = room.get(idx);
                    for(String user : roomUser){
                        sendMsg(user);
                    }
                }else if(msg.startsWith("/whisper")){
                    whisper(msg);
                }
                else{
                    System.out.println(id + msg+"broadcast" );
                    broadcast(idx, id+": "+msg);
//                    pw.println(id+": "+msg);
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            chatClients.remove(this.id);
            broadcast(id+"님이 나갔습니다.");
            System.out.println(id+"닉네임의 사용자가 연결을 끊었습니다.");

            if(out!=null){
                out.close();
            }
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if(socket!=null){
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
//            pw.close();
        }

    }

    private void whisper(String msg) {
        int firstIndex = msg.indexOf(" ");
        if(firstIndex == -1)return;
        int secondIndex = msg.indexOf(" ", firstIndex+1);
        if(secondIndex == 1)return;
        String to = msg.substring(firstIndex+1, secondIndex);
        String message = msg.substring(secondIndex+1);
        PrintWriter pw = chatClients.get(to);
        if(pw!=null){
            pw.println("(귓속말)"+id+": "+message);

        }else{
            sendMsg(to+"유저가 현재 접속중이지 않거나 없는 유저입니다.");
        }
    }

    //전체발신
    public void broadcast(String msg){
        synchronized (chatClients){
            Iterator<PrintWriter> iter = chatClients.values().iterator();
            while(iter.hasNext()){
                PrintWriter pw = iter.next();
                try{
                    pw.println(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    //한명에게 발신
    public void sendMsg(String msg){
        //to(수신자)에게 메세지전송
        PrintWriter pw = chatClients.get(id);
        if(pw!=null){
            pw.println(msg);
        }
    }
    //톡방사람들끼리 발신
    public void broadcast(int roomId , String msg){
        List<String> participants = room.get(roomId);//id에 해당하는 닉네임 구하기
        System.out.println("participants: "+participants);
        if(participants!=null){
            synchronized (chatClients){
                for(String participant : participants){//닉네임
                    PrintWriter pw = chatClients.get(participant);
                    if(pw!=null){
                        try{
                            pw.println(msg);
                            // 대화를 파일에 기록

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                try{
                    pw = new PrintWriter(new FileWriter("roomTalk" + roomId + ".txt", true));
                    pw.println(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    pw.close();
                }
            }
        }
    }


}