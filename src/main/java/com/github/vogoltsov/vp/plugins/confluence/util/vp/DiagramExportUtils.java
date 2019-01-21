package com.github.vogoltsov.vp.plugins.confluence.util.vp;

import com.github.vogoltsov.vp.plugins.confluence.client.ConfluenceAttachmentRepository;
import com.github.vogoltsov.vp.plugins.confluence.client.model.Attachment;
import com.vp.plugin.ApplicationManager;
import com.vp.plugin.ExportDiagramAsImageOption;
import com.vp.plugin.diagram.IDiagramUIModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;

/**
 * @author Vitaly Ogoltsov &lt;vitaly.ogoltsov@me.com&gt;
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DiagramExportUtils {

    public static Attachment export(IDiagramUIModel diagram, String pageId, String attachmentId) {
        // export diagram as image
        byte[] imageData;
        try {
            imageData = exportDiagramAsImage(diagram);
        } catch (Exception e) {
            String message = "Unexpected exception while saving diagram as image";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                message += ": " + e.getMessage();
            }
            throw new RuntimeException(message);
        }
        // upload attachment to confluence
        Attachment attachment;
        try {
            String name = diagram.getName() + ".png";
            if (attachmentId == null) {
                attachment = ConfluenceAttachmentRepository.getInstance().create(pageId, name, imageData);
            } else {
                attachment = ConfluenceAttachmentRepository.getInstance().update(pageId, attachmentId, name, imageData);
            }
        } catch (Exception e) {
            String message = "Unexpected exception while uploading diagram to Confluence";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                message += ": " + e.getMessage();
            }
            throw new RuntimeException(message);
        }
        // return attachment
        return attachment;
    }

    private static byte[] exportDiagramAsImage(IDiagramUIModel diagram) throws Exception {
        return exportDiagramAsImage(diagram, getDefaultExportOption());
    }

    private static byte[] exportDiagramAsImage(IDiagramUIModel diagram, ExportDiagramAsImageOption option) throws Exception {
        Image image = ApplicationManager.instance().getModelConvertionManager().exportDiagramAsImage(diagram, option);
        // get rendered image
        RenderedImage renderedImage;
        if (image instanceof RenderedImage) {
            renderedImage = (RenderedImage) image;
        } else {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            bufferedImage.getGraphics().drawImage(image, 0, 0, null);
            renderedImage = bufferedImage;
        }
        // save image as byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(renderedImage, "png", baos);
        return baos.toByteArray();
    }

    private static ExportDiagramAsImageOption getDefaultExportOption() {
        ExportDiagramAsImageOption option = new ExportDiagramAsImageOption(ExportDiagramAsImageOption.IMAGE_TYPE_PNG);
        option.setScale(2.0f);
        option.setTextAntiAliasing(true);
        option.setGraphicAntiAliasing(true);
        return option;
    }

}
