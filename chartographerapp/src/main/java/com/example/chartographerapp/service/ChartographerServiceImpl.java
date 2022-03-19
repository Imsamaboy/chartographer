package com.example.chartographerapp.service;

import com.example.chartographerapp.entity.Charta;
import com.example.chartographerapp.entity.Fragment;
import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import com.example.chartographerapp.repository.ChartoRepository;
import com.example.chartographerapp.repository.FragmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.chartographerapp.ChartographerApplication.contentPath;

@Service
public class ChartographerServiceImpl implements ChartographerService {

    private final ChartoRepository chartoRepository;

    private final FragmentRepository fragmentRepository;

    @Autowired
    public ChartographerServiceImpl(ChartoRepository repository, FragmentRepository fragmentRepository) {
        this.chartoRepository = repository;
        this.fragmentRepository = fragmentRepository;
    }

    public boolean checkXAndY(Charta charta, Integer x, Integer y) {
        return x <= charta.getWidth() && y <= charta.getHeight();
    }

    public List<Fragment> createFragmentsListByChartaParams(Integer width, Integer height, Charta charta) throws IOException {
        String path = contentPath == null ? "src/main/resources/static": contentPath;
        List<Fragment> fragments = new ArrayList<>();
        for (int y = 0; y < height; y += Math.min(5_000, height - y)) {
            for (int x = 0; x < width; x += Math.min(5_000, width - x)) {

                Integer fragmentId =  fragmentRepository.findCurrentFragmentId().map(integer -> integer + 1).orElse(1);

                int w =  Math.min(5_000, width - x);
                int h =  Math.min(5_000, height - y);
                String fileName = path + "/" + fragmentId;

                BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
                File file = new File(fileName);
                ImageIO.write(image, "bmp", file);

                Fragment fragment = new Fragment(fragmentId, x, y, w, h, fileName, charta);
                // Сохраняем фрагмент в бд
                fragmentRepository.save(fragment);
                // Добавляем фрагмент в список
                fragments.add(fragment);
            }
        }
        return fragments;
    }

    @Override
    public String createCharta(Integer width, Integer height) throws IOException {
//        // Переписать логику с path
//        String path = "/home/sfelshtyn/Documents/chartographerapp/src/main/resources/static";
        // Находим следующий айдишник для новой харты
        Integer id = chartoRepository.findCurrentChartaId().map(integer -> integer + 1).orElse(1);
        // Создаём харту и сохраняем её
        Charta charta = new Charta(id, width, height, null);    // path + "/" + id
        chartoRepository.save(charta);

        // Создаём лист фрагментов под новую харту
        List<Fragment> fragments = createFragmentsListByChartaParams(width, height, charta);
        charta.setFragmentList(fragments);

        chartoRepository.save(charta);
        return String.valueOf(charta.getId());
    }

    @Override
    public void deleteCharta(Integer id) throws ChartaNotFoundException {
        Optional<Charta> chartaOrNull = chartoRepository.getChartaById(id);
        if (chartaOrNull.isPresent()) {
            Charta charta = chartaOrNull.get();
            List<Fragment> fragmentsByCharta = fragmentRepository.findFragmentByCharta(charta);
            for (Fragment fragment: fragmentsByCharta) {
                System.out.println(new File(fragment.getFileName()).delete() ? "Файл удалён" : "Файл не найден");
                fragmentRepository.delete(fragment);
            }
            chartoRepository.delete(charta);
        } else {
            throw new ChartaNotFoundException("Charta not found with this id");
        }
    }

    @Override
    public void addFragmentInCharta(Integer id, Integer width, Integer height, Integer x, Integer y, byte[] array)
            throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException {

        // Входная картинка
        ByteArrayInputStream stream = new ByteArrayInputStream(array);
        BufferedImage inputImage = ImageIO.read(stream);

        // Достаём харту из бд
        Optional<Charta> chartaOrNull = chartoRepository.getChartaById(id);
        if (chartaOrNull.isPresent()) {
            Charta charta = chartaOrNull.get();
            List<Fragment> fragmentsByCharta = fragmentRepository.findFragmentByCharta(charta);
            if (!checkXAndY(charta, x, y)) {
                throw new FragmentNotCrossingChartaException("X or Y is incorrect");
            } else {
                List<Fragment> fragmentsThatCrossing = findFragmentsThatCrossingImage(fragmentsByCharta, x, y, width, height);
                if (fragmentsThatCrossing.isEmpty()){
                    throw new FragmentNotCrossingChartaException("");
                } else {
                    for (Fragment fragment: fragmentsThatCrossing) {
                        Map<String, Integer> points = findPoints(fragment, x, y, width, height);
                        BufferedImage fragmentImage = ImageIO.read(new File(fragment.getFileName()));
                        fragmentImage.createGraphics().drawImage(
                                inputImage,
                                points.get("startX") - fragment.getX(),
                                points.get("startY") - fragment.getY(),
                                points.get("endX") - points.get("startX"),
                                points.get("endY") - points.get("startY"),
                                null
                        );
                        ImageIO.write(fragmentImage, "bmp", new File(fragment.getFileName()));
                    }
                }
            }
        } else {
            throw new ChartaNotFoundException("Charta not found with this id");
        }
    }

    @Override
    public byte[] getFragmentInCharta(Integer id, Integer x, Integer y, Integer width, Integer height) throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException {
        Optional<Charta> chartaOrNull = chartoRepository.getChartaById(id);
        if (chartaOrNull.isPresent()) {
            Charta charta = chartaOrNull.get();
            List<Fragment> fragmentsByCharta = fragmentRepository.findFragmentByCharta(charta);
            if (!checkXAndY(charta, x, y)) {
                throw new FragmentNotCrossingChartaException("X or Y is incorrect");
            } else {
                List<Fragment> fragmentsThatCrossing = findFragmentsThatCrossingImage(fragmentsByCharta, x, y, width, height);
                if (fragmentsThatCrossing.isEmpty()){
                    throw new FragmentNotCrossingChartaException("");
                } else {
                    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D graphics2D = result.createGraphics();
                    graphics2D.setColor(Color.BLACK);
                    graphics2D.fillRect(0,0,width,height);
                    for (Fragment fragment: fragmentsThatCrossing) {
                        Map<String, Integer> points = findPoints(fragment, x, y, width, height);
                        BufferedImage fragmentSubImage = ImageIO.read(new File(fragment.getFileName())).getSubimage(
                                points.get("startX") - fragment.getX(),
                                points.get("startY") - fragment.getY(),
                                points.get("endX") - points.get("startX"),
                                points.get("endY") - points.get("startY"));
                        result.createGraphics().drawImage(
                                fragmentSubImage,
                                points.get("startX") - x,
                                points.get("startY") - y,
                                points.get("endX") - points.get("startX"),
                                points.get("endY") - points.get("startY"),
                                null);
                    }
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ImageIO.write(result, "bmp", stream);
                    return stream.toByteArray();
                }
            }
        } else {
            throw new ChartaNotFoundException("Charta not found");
        }
    }

    private Map<String, Integer> findPoints(Fragment fragment, Integer x, Integer y, Integer width, Integer height) {
        int startX = Math.max(x, fragment.getX());
        int startY = Math.max(y, fragment.getY());
        int endX = Math.min(x + width, fragment.getX() + fragment.getWidth());
        int endY = Math.min(y + height, fragment.getY() + fragment.getHeight());
        return Map.of(
          "startX", startX,
          "startY", startY,
          "endX", endX,
          "endY", endY
        );
    }

    private List<Fragment> findFragmentsThatCrossingImage(List<Fragment> fragmentsByCharta,
                                                          Integer x,
                                                          Integer y,
                                                          Integer width,
                                                          Integer height) {
        return fragmentsByCharta.stream()
                .filter(fragment -> fragment.getX() < x + width
                        && x < fragment.getX() + fragment.getWidth()
                        && fragment.getY() < y + height
                        && y < fragment.getY() + fragment.getHeight())
                .collect(Collectors.toList());
    }

}

//                        int startX = Math.max(x, fragment.getX());
//                        int startY = Math.max(y, fragment.getY());
//                        int endX = Math.min(x + width, fragment.getX() + fragment.getWidth());
//                        int endY = Math.min(y + height, fragment.getY() + fragment.getHeight());
