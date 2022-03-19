package com.example.chartographerapp.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Fragment")
public class Fragment {
    @Id
    @Column(name = "fragment_id", nullable = false)
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "file_name")
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charta_id")
    @ToString.Exclude
    private Charta charta;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Fragment fragment = (Fragment) o;
        return id != null && Objects.equals(id, fragment.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Fragment(Integer x, Integer y, Integer width, Integer height, String fileName, Charta charta) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fileName = fileName;
        this.charta = charta;
    }
}
