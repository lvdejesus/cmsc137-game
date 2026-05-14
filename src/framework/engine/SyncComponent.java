package framework.engine;

public interface SyncComponent extends Component {
    void fromBytes(byte[] bytes);
    byte[] toBytes();

    boolean isEqual(SyncComponent other);
    void copyFrom(SyncComponent other);

    SyncComponent clone();
}
