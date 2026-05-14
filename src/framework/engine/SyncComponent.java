package framework.engine;

public interface SyncComponent extends Component {
    void fromBytes(byte[] bytes);
    byte[] toBytes();
}
