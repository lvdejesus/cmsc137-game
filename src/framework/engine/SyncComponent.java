package framework.engine;

public interface SyncComponent extends Component {
    void syncFromBytes(byte[] bytes);
}
