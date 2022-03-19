package com.example.chartographerapp.entity;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Charta")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Charta {
    @Id
    @Column(name = "charta_id", nullable = false, updatable = false)
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;

    @OneToMany(mappedBy = "charta")
    @ToString.Exclude
    private List<Fragment> fragmentList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Charta charta = (Charta) o;
        return id != null && Objects.equals(id, charta.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Charta(Integer width, Integer height, List<Fragment> fragmentList) {
        this.width = width;
        this.height = height;
        this.fragmentList = fragmentList;
    }
}
