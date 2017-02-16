package quant.test.server.log;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/2/16.
 */
public class LogProcessor extends Thread {
    private final LinkedList<LogItem> logItems=new LinkedList<>();
    private final List<PipedOutputStream> pipedStreams=new ArrayList<>();
    public LogProcessor() {
        super("LogThread");
    }

    @Override
    public void run() {
        super.run();
        while(true){
            synchronized (logItems){
                try {
                    while(!logItems.isEmpty()){
                        LogItem logItem = logItems.pollFirst();
                        final String value="logItem:"+logItem.threadName+" "+logItem.tag+" "+logItem.value+" size:"+logItems.size();
                        System.out.println(value);
                        pipedStreams.forEach(stream->{
                            byte[] bytes = value.getBytes();
                            try {
                                stream.write(bytes,0,bytes.length);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    logItems.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void process(LogItem item) {
        synchronized (logItems){
            logItems.addLast(item);
            logItems.notify();
        }
    }

    public PipedInputStream newPipedStream() {
        PipedInputStream pipedInputStream=new PipedInputStream();
        PipedOutputStream pipedOutputStream=new PipedOutputStream();
        try {
            pipedInputStream.connect(pipedOutputStream);
            pipedStreams.add(pipedOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pipedInputStream;
    }

    public void destroyStream(){
        if(!pipedStreams.isEmpty()){
            pipedStreams.forEach(s -> {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
