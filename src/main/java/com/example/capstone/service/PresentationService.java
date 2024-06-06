package com.example.demo.Service;

import com.example.demo.Domain.*;
import com.example.demo.Domain.Shape;
import com.example.demo.Repository.PresentationFileRepository;
import com.example.demo.Repository.PresentationRepository;
import com.example.demo.dto.PresentationRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
public class PresentationService {

    private static final Logger log = LoggerFactory.getLogger(PresentationService.class);

    private final PresentationRepository presentationRepository;
    private final ImageStore imageStore;
    private final S3Store s3Store;
    private final PresentationFileRepository presentationFileRepository;


    public Presentation createShortPPT(PresentationRequest request) {
        Presentation newPresentation = new Presentation();
        newPresentation.setPresentationType("short");
        List<Presentation> presentations = presentationRepository.findByPresentationType("short");

        List<Map<String, Object>> contentsList = convertContentsToList(request);

        log.info("Final contentsList: {}", contentsList);

        if (!presentations.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(presentations.size());
            Presentation selectedPresentation = presentations.get(randomIndex);

            log.info("Selected presentation with id: {}", selectedPresentation.getPresentation_id());

            for (Slide oldSlide : selectedPresentation.getSlides()) {
                Slide newSlide = cloneSlide(oldSlide, contentsList);
                newSlide.setPresentation(newPresentation);
                newPresentation.getSlides().add(newSlide);
                log.info("Added new slide to presentation");
            }
        } else {
            log.info("No presentations found with type 'short'");
        }
        return newPresentation;
    }

    private List<Map<String, Object>> convertContentsToList(PresentationRequest request) {
        List<Map<String, Object>> contentsList = new ArrayList<>();

        if (request.getTitle() != null) {
            Map<String, Object> titleContent = new HashMap<>();
            titleContent.put("type", "title");
            titleContent.put("content", Collections.singletonList(request.getTitle()));
            contentsList.add(titleContent);
        }
        if (request.getStartDate() != null && request.getEndDate() != null && request.getTeamNum() > 0) {
            Map<String, Object> durationAndTeamContent = new HashMap<>();
            durationAndTeamContent.put("type", "duration");
            String durationAndTeam = request.getStartDate() + " ~ " + request.getEndDate() + " / " + request.getTeamNum() + "명";
            durationAndTeamContent.put("content", Collections.singletonList(durationAndTeam));
            contentsList.add(durationAndTeamContent);
        }
        if (request.getDescription() != null) {
            Map<String, Object> descriptionContent = new HashMap<>();
            descriptionContent.put("type", "subdetail");
            descriptionContent.put("content", Collections.singletonList(request.getDescription()));
            contentsList.add(descriptionContent);
        }
        if (request.getContribution() != null) {
            Map<String, Object> contributionContent = new HashMap<>();
            contributionContent.put("type", "role");
            contributionContent.put("content", Collections.singletonList(request.getContribution()));
            contentsList.add(contributionContent);
        }
        if (request.getImage() != null) {
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image");
            imageContent.put("content", Collections.singletonList(request.getImage()));
            contentsList.add(imageContent);
        }
        if (request.getTechStack() != null && !request.getTechStack().isEmpty()) {
            Map<String, Object> techStackContent = new HashMap<>();
            techStackContent.put("type", "techStack");
            String techStackString = String.join(", ", request.getTechStack());
            techStackContent.put("content", Collections.singletonList(techStackString));
            contentsList.add(techStackContent);
        }
        return contentsList;
    }


    private Slide cloneSlide(Slide oldSlide, List<Map<String, Object>> contentsList) {
        Slide newSlide = new Slide();
        newSlide.setTheme_color(oldSlide.getTheme_color());
        newSlide.setSecondary_color(oldSlide.getSecondary_color());
        newSlide.setSlide_type(oldSlide.getSlide_type());

        log.info("Cloning slide with type: {}", oldSlide.getSlide_type());

        List<TextBox> textBoxes = oldSlide.getTextBoxes().stream()
                .map(TextBox::cloneTextBox)
                .collect(Collectors.toList());

        log.info("assignTextBoxContents must do : {}", textBoxes);

        assignTextContents(textBoxes, contentsList);

        List<ImageBox> imageBoxes = oldSlide.getImageBoxes().stream()
                .map(ImageBox::cloneImageBox)
                .collect(Collectors.toList());

        assignImageBoxUrls(imageBoxes, contentsList);

        List<Shape> shapes = oldSlide.getShapes().stream()
                .map(Shape::cloneShape)
                .collect(Collectors.toList());

        newSlide.setTextBoxes(textBoxes);
        newSlide.setImageBoxes(imageBoxes);
        newSlide.setShapes(shapes);

        log.info("Create completely a slide");

        return newSlide;
    }
    // textBox에는 여러가지 종류 존재 constant(text값 보유) 와 그외의 타입들(text값 없음/null)
    private void assignTextContents(List<TextBox> textComponents, List<Map<String, Object>> contents) {
        for (TextBox textBox : textComponents) {
            if (!"constant".equals(textBox.getBox_type())) {
                for (Map<String, Object> content : contents) {
                    String type = (String) content.get("type");
                    List<String> textContents = (List<String>) content.get("content");

                    if (textContents != null && !textContents.isEmpty() && (textBox.getBox_type().equals(type))) {
                        textBox.setText(textContents.get(0));
                        break;
                    }
                }
            }
        }
    }

    private void assignImageBoxUrls(List<ImageBox> imageComponents, List<Map<String, Object>> contents) {
        Iterator<Map<String, Object>> iterator = contents.iterator();
        for (ImageBox imageBox : imageComponents) {
            while (iterator.hasNext()) {
                Map<String, Object> content = iterator.next();
                String type = (String) content.get("type");
                List<String> imageUrls = (List<String>) content.get("image");

                if (imageUrls != null && !imageUrls.isEmpty() && ("title".equals(type) || "content".equals(type))) {
                    imageBox.setImage_url(imageUrls.remove(0));
                    break;
                }
            }
        }
    }


    public byte[] createPowerPoint(Presentation presentation) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XMLSlideShow ppt = new XMLSlideShow();
            Dimension slideSize = new Dimension(960, 480);
            ppt.setPageSize(slideSize);

            for (Slide slide : presentation.getSlides()) {
                XSLFSlide pptSlide = ppt.createSlide();
                pptSlide.getSlideShow().setPageSize(slideSize);
                Color themeColor = getColorFromHex(slide.getTheme_color());
                applySlideBackgroundColor(pptSlide, themeColor);

                for (TextBox textBox : slide.getTextBoxes()) {
                    log.info("Creating TextBox: {}", textBox);
                    createTextBox(pptSlide, textBox);
                }
                for (ImageBox imageBox : slide.getImageBoxes()) {
                    createImageBox(ppt, pptSlide, imageBox);
                }
                for (Shape shape : slide.getShapes()) {
                    createShape(pptSlide, shape);
                }
            }
            ppt.write(out);
            log.info("Presentation created successfully in memory!");
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error while creating the PowerPoint file in memory: {}", e.getMessage());
            throw new IllegalStateException("Failed to create PowerPoint file", e);
        }
    }

    private Color getColorFromHex(String hexColor) {
        if (hexColor == null || hexColor.isEmpty() || "undefined".equals(hexColor)) {
            return null;
        }
        return new Color(
                Integer.valueOf(hexColor.substring(1, 3), 16),
                Integer.valueOf(hexColor.substring(3, 5), 16),
                Integer.valueOf(hexColor.substring(5, 7), 16)
        );
    }

    private void applySlideBackgroundColor(XSLFSlide slide, Color color) {
        XSLFBackground background = slide.getBackground();
        background.setFillColor(color);
    }

    private void createTextBox(XSLFSlide slide, TextBox textBox) {
        log.info("Creating TextBox with the following details - Position X: {}, Position Y: {}, Width: {}, Height: {}, Text: {}, Font Family: {}, Font Size: {}, Is Bold: {}, Is Italic: {}, Text Color: {}, Text Align: {}",
                textBox.getPosition_x(), textBox.getPosition_y(), textBox.getWidth(), textBox.getHeight(), textBox.getText(), textBox.getFont_family(), textBox.getFont_size(), textBox.getIs_bold(), textBox.getIs_italic(), textBox.getText_color(), textBox.getText_align());

        XSLFTextBox pptTextBox = slide.createTextBox();
        pptTextBox.setAnchor(new Rectangle2D.Double(textBox.getPosition_x(), textBox.getPosition_y(), textBox.getWidth(), textBox.getHeight()));
        XSLFTextParagraph paragraph = pptTextBox.addNewTextParagraph();

        String textAlign = textBox.getText_align() != null ? textBox.getText_align().toLowerCase() : "left";
        log.info("Setting text align for TextBox: {}", textAlign);
        switch (textAlign) {
            case "center":
                paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
                break;
            case "left":
                paragraph.setTextAlign(TextParagraph.TextAlign.LEFT);
                break;
            case "right":
                paragraph.setTextAlign(TextParagraph.TextAlign.RIGHT);
                break;
            case "justify":
                paragraph.setTextAlign(TextParagraph.TextAlign.JUSTIFY);
                break;
            default:
                log.warn("Unknown text align value '{}', defaulting to LEFT", textAlign);
                paragraph.setTextAlign(TextParagraph.TextAlign.LEFT);
                break;
        }

        XSLFTextRun run = paragraph.addNewTextRun();
        run.setText(textBox.getText());
        run.setFontSize(textBox.getFont_size());
        run.setBold(textBox.getIs_bold());
        run.setItalic(textBox.getIs_italic());
        run.setFontFamily(textBox.getFont_family());
        if (textBox.getText_color() != null && !textBox.getText_color().isEmpty()) {
            Color color = Color.decode(textBox.getText_color());
            run.setFontColor(color);
        }
        log.info("Created text box with text: {}", textBox.getText());
    }

    private void createImageBox(XMLSlideShow ppt, XSLFSlide slide, ImageBox imageBox) {
        byte[] pictureData = new byte[0];
        BufferedImage bufferedImage = null;
        try {
            if (imageBox.getImage_url().startsWith("http")) {
                InputStream imageStream = s3Store.findImageBytes(imageBox.getImage_url());
                pictureData = toByteArray(imageStream);
                bufferedImage = ImageIO.read(new ByteArrayInputStream(pictureData));
            } else {
                File imageFile = new File(imageStore.getFullPath(imageBox.getImage_url()));
                pictureData = Files.readAllBytes(imageFile.toPath());
                bufferedImage = ImageIO.read(imageFile);
            }
        } catch (IOException e) {
            log.error("Error loading image from URL: {}", imageBox.getImage_url(), e);
        }

        if (bufferedImage != null) {
            int originalWidth = bufferedImage.getWidth();
            int originalHeight = bufferedImage.getHeight();
            double aspectRatio = (double) originalWidth / originalHeight;

            double targetWidth = imageBox.getWidth();
            double targetHeight = imageBox.getHeight();
            if (targetWidth / aspectRatio > targetHeight) {
                targetWidth = targetHeight * aspectRatio;
            } else {
                targetHeight = targetWidth / aspectRatio;
            }

            XSLFPictureData idx = ppt.addPicture(pictureData, PictureData.PictureType.PNG);
            XSLFPictureShape pic = slide.createPicture(idx);
            pic.setAnchor(new Rectangle2D.Double(imageBox.getPosition_x(), imageBox.getPosition_y(), targetWidth, targetHeight));
            log.debug("Created image box with URL: {}", imageBox.getImage_url());
        } else {
            log.error("BufferedImage is null for URL: {}", imageBox.getImage_url());
        }
    }

    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }


    private void createShape(XSLFSlide slide, Shape shape) {
        XSLFAutoShape pptShape = slide.createAutoShape();
        pptShape.setShapeType(ShapeType.valueOf(shape.getShape_type()));
        pptShape.setAnchor(new Rectangle2D.Double(shape.getPosition_x(), shape.getPosition_y(), shape.getWidth(), shape.getHeight()));
        if (shape.getShape_color() != null && !shape.getShape_color().isEmpty()) {
            try {
                Color color = Color.decode(shape.getShape_color());
                pptShape.setFillColor(color);
            } catch (NumberFormatException e) {
                log.error("Invalid color format for shape: {}", shape.getShape_color(), e);
                pptShape.setFillColor(Color.WHITE);
            }
        } else {
            pptShape.setFillColor(Color.WHITE);
        }
        log.debug("Created shape of type: {}", shape.getShape_type());
    }

    public void savePresentationFile(byte[] fileData, Long userId) {
        PresentationFile presentationFile = new PresentationFile();
        presentationFile.setUserId(userId);
        presentationFile.setFileData(fileData);
        presentationFileRepository.save(presentationFile);
    }

    public List<PresentationFile> getPresentationFilesByUserId(Long userId) {
        return presentationFileRepository.findByUserId(userId);
    }
}
