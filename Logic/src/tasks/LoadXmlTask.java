/*
package tasks;

import Lib.RepositoryManager;
import Lib.XMLChecker;
import javafx.application.Platform;
import javafx.concurrent.Task;
import resources.generated.MagitRepository;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class LoadXmlTask extends Task<Boolean> {
    private String xmlPath;
    private Consumer<List<String>> notValidXmlDeligate;
    private RepositoryManager repositoryManager;
    private Function<Object, Boolean> handleWithExistDelegate;

    public LoadXmlTask(String i_xmlPath, Consumer<List<String>> handleWithInvalidXml, Function<Object, Boolean> alreadyRepositoryExist, RepositoryManager repositoryManager) {
        xmlPath = i_xmlPath;
        notValidXmlDeligate = handleWithInvalidXml;
        this.repositoryManager = repositoryManager;
        handleWithExistDelegate = alreadyRepositoryExist;
    }

    @Override
    protected Boolean call() throws Exception {
        final Boolean[] result = new Boolean[1];
        updateMessage("Checking xml file existing...");
        updateProgress(0, 1);
        XMLChecker.isXMLFile(xmlPath);
        updateProgress(0.5, 1);
        XMLChecker.isFileExist(xmlPath);
        updateProgress(1, 1);
        updateMessage("Generate objects...");
        updateProgress(0, 1);
        InputStream inputStream = new FileInputStream(xmlPath);
        MagitRepository magitRepository = XMLChecker.deserializeFrom(inputStream);
        updateProgress(1, 1);

        updateMessage("Checking xml validation...");
        updateProgress(0, 1);
        List<String> errors = XMLChecker.CheckXML(magitRepository);
        if (!errors.isEmpty()) {
            Platform.runLater(() -> notValidXmlDeligate.accept(errors));
            updateMessage("Failed loading xml file...");
            return Boolean.FALSE;
        }
        updateProgress(1, 1);

        if (repositoryManager.IsRepositoryExist(magitRepository.getLocation())) {
            Platform.runLater(() -> {
                result[0] = handleWithExistDelegate.apply(null);
            });
            if (!result[0]) {
                return Boolean.FALSE;
            }
        }
        updateMessage("Loading repository...");
        updateProgress(0, 1);
        repositoryManager.changeMagitRepositoryToRepository(magitRepository);
        updateProgress(1, 1);
        return Boolean.TRUE;
    }


}
*/
