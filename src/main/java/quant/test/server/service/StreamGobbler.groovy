package quant.test.server.service
/**
 * Created by cz on 2017/2/22.
 */
class StreamGobbler extends Thread{
    InputStream inputStream
    def closure

    public StreamGobbler(InputStream inputStream, closure) {
        this.inputStream = inputStream
        this.closure = closure
    }
    @Override
    void run() {
        super.run()
        BufferedReader br
        try {
            String line
            br = new BufferedReader(new InputStreamReader(inputStream))
            while ((line = br.readLine()) != null) {
                closure?.call(line)
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            br?.close()
        }
    }
}
