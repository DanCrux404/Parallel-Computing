package basicconsumerproducer;

import basicconsumerproducer.models.Consumer;
import basicconsumerproducer.models.Container;
import basicconsumerproducer.models.Producer;

/**
 *
 * @author dante
 */
public class BasicConsumerProducer {

    public static void main(String[] args) {
        System.out.println("aaaaaaaaaaaa");
        Container c = new Container();
        Producer produce = new Producer(c);
        Consumer consume = new Consumer(c);

        produce.start();
        consume.start();
    }
}