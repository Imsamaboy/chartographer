package com.example.chartographerapp.service;

import com.example.chartographerapp.dto.ChartaFragmentDto;
import com.example.chartographerapp.dto.CreateChartaDto;
import com.example.chartographerapp.dto.GetChartaDto;
import com.example.chartographerapp.entity.Charta;
import com.example.chartographerapp.entity.Fragment;
import com.example.chartographerapp.exception.ChartaNotFoundException;
import com.example.chartographerapp.exception.FragmentNotCrossingChartaException;
import com.example.chartographerapp.repository.ChartoRepository;
import com.example.chartographerapp.repository.FragmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.chartographerapp.ChartographerApplication.contentPath;

@Service
@Slf4j
public class ChartographerService {

    private final ChartoRepository chartoRepository;

    private final FragmentRepository fragmentRepository;

    public static final String START_X = "startX";
    public static final String START_Y = "startY";
    public static final String END_X = "endX";
    public static final String END_Y = "endY";

    @Autowired
    public ChartographerService(ChartoRepository repository, FragmentRepository fragmentRepository) {
        this.chartoRepository = repository;
        this.fragmentRepository = fragmentRepository;
    }

    @Transactional
    public String createCharta(CreateChartaDto chartaDto) throws IOException {
        // Находим следующий айдишник для новой харты
        Integer id = chartoRepository.findCurrentChartaId().map(integer -> integer + 1).orElse(1);
        // Создаём харту и сохраняем её
        Charta charta = new Charta(id, chartaDto.getWidth(), chartaDto.getHeight(), null);
        chartoRepository.save(charta);
        // Создаём лист фрагментов под новую харту
        List<Fragment> fragments = createFragmentsListByChartaParams(chartaDto, charta);
        charta.setFragmentList(fragments);
        chartoRepository.save(charta);
        return String.valueOf(charta.getId());
    }

    @Transactional
    public void deleteCharta(Integer id) throws ChartaNotFoundException {
        Charta charta = chartoRepository.getChartaById(id).orElseThrow(() -> new ChartaNotFoundException(id));
        List<Fragment> fragmentsByCharta = fragmentRepository.findFragmentByCharta(charta);
        for (Fragment fragment: fragmentsByCharta) {
            log.error(new File(fragment.getFileName()).delete() ? "Файл удалён" : "Файл не найден");
            fragmentRepository.delete(fragment);
        }
        chartoRepository.delete(charta);
    }
    @Transactional
    public void addFragmentInCharta(Integer id, ChartaFragmentDto chartaDto, byte[] array)
            throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException {
        // Входная картинка
        ByteArrayInputStream stream = new ByteArrayInputStream(array);
        BufferedImage inputImage = ImageIO.read(stream);

        // Достаём харту из бд
        Charta charta = chartoRepository.getChartaById(id).orElseThrow(() -> new ChartaNotFoundException(id));
        List<Fragment> fragmentsByCharta = fragmentRepository.findFragmentByCharta(charta);
        if (!checkXAndY(charta, chartaDto.getX(), chartaDto.getY())) {
            throw new FragmentNotCrossingChartaException(chartaDto.getX(), chartaDto.getY());
        } else {
            List<Fragment> fragmentsThatCrossing = findFragmentsThatCrossingImage(fragmentsByCharta, chartaDto);
            if (fragmentsThatCrossing.isEmpty()) {
                throw new FragmentNotCrossingChartaException(chartaDto.getX(), chartaDto.getY());
            } else {
                for (Fragment fragment: fragmentsThatCrossing) {
                    Map<String, Integer> points = findPoints(fragment, chartaDto);
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
    }

    public byte[] getFragmentInCharta(Integer id, GetChartaDto chartaDto) throws IOException, ChartaNotFoundException, FragmentNotCrossingChartaException {
        Charta charta = chartoRepository.getChartaById(id).orElseThrow(() -> new ChartaNotFoundException(id));
        List<Fragment> fragmentsByCharta = fragmentRepository.findFragmentByCharta(charta);
        if (!checkXAndY(charta, chartaDto.getX(), chartaDto.getY())) {
            throw new FragmentNotCrossingChartaException(chartaDto.getX(), chartaDto.getY());
        }
        List<Fragment> fragmentsThatCrossing = findFragmentsThatCrossingImage(fragmentsByCharta, chartaDto);
        if (fragmentsThatCrossing.isEmpty()) {
            throw new FragmentNotCrossingChartaException(chartaDto.getX(), chartaDto.getY());
        }
        BufferedImage result = new BufferedImage(chartaDto.getWidth(), chartaDto.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics2D = result.createGraphics();
        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0,0, chartaDto.getWidth(), chartaDto.getHeight());
        for (Fragment fragment: fragmentsThatCrossing) {
            Map<String, Integer> points = findPoints(fragment, chartaDto);
            BufferedImage fragmentSubImage = ImageIO.read(new File(fragment.getFileName())).getSubimage(
                    points.get("startX") - fragment.getX(),
                    points.get("startY") - fragment.getY(),
                    points.get("endX") - points.get("startX"),
                    points.get("endY") - points.get("startY"));
            result.createGraphics().drawImage(
                    fragmentSubImage,
                    points.get("startX") - chartaDto.getX(),
                    points.get("startY") - chartaDto.getY(),
                    points.get("endX") - points.get("startX"),
                    points.get("endY") - points.get("startY"),
                    null);
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(result, "bmp", stream);
        return stream.toByteArray();
    }

    private List<Fragment> createFragmentsListByChartaParams(CreateChartaDto chartaDto, Charta charta) throws IOException {
        return createFragmentsListByChartaParams(chartaDto.getWidth(), chartaDto.getHeight(), charta);
    }

    private List<Fragment> createFragmentsListByChartaParams(Integer width, Integer height, Charta charta) throws IOException {
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

    private Map<String, Integer> findPoints(Fragment fragment, GetChartaDto chartaDto) {
        return findPoints(fragment, chartaDto.getX(), chartaDto.getY(), chartaDto.getWidth(), chartaDto.getHeight());
    }

    private Map<String, Integer> findPoints(Fragment fragment, ChartaFragmentDto chartaDto) {
        return findPoints(fragment, chartaDto.getX(), chartaDto.getY(), chartaDto.getWidth(), chartaDto.getHeight());
    }

    private Map<String, Integer> findPoints(Fragment fragment, Integer x, Integer y, Integer width, Integer height) {
        int startX = Math.max(x, fragment.getX());
        int startY = Math.max(y, fragment.getY());
        int endX = Math.min(x + width, fragment.getX() + fragment.getWidth());
        int endY = Math.min(y + height, fragment.getY() + fragment.getHeight());
        return Map.of(
          START_X, startX,
          START_Y, startY,
          END_X, endX,
          END_Y, endY
        );
    }

    private List<Fragment> findFragmentsThatCrossingImage(List<Fragment> fragmentsByCharta, ChartaFragmentDto chartaFragmentDto) {
        return findFragmentsThatCrossingImage(
                fragmentsByCharta,
                chartaFragmentDto.getX(),
                chartaFragmentDto.getY(),
                chartaFragmentDto.getWidth(),
                chartaFragmentDto.getHeight());
    }

    private List<Fragment> findFragmentsThatCrossingImage(List<Fragment> fragmentsByCharta, GetChartaDto getChartaDto) {
        return findFragmentsThatCrossingImage(
                fragmentsByCharta,
                getChartaDto.getX(),
                getChartaDto.getY(),
                getChartaDto.getWidth(),
                getChartaDto.getHeight());
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

    private boolean checkXAndY(Charta charta, Integer x, Integer y) {
        return x <= charta.getWidth() && y <= charta.getHeight();
    }
}
