package net.huray.bloodgc.model;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;



@Entity
abstract class AbstractBloodGlucose {
    @Key
    @Generated
    long id;

    // 검사시간 milisecond
    @Column(nullable = false)
    String recordDttm;

    // 검사 혈당치
    @Column(nullable = false)
    int measure;

    // 식전, 식후 구별 true 식후
    @Column(nullable = false)
    boolean ateFood;

    @Override
    public boolean equals(Object obj) {
        BloodGlucose glucose = (BloodGlucose)obj;
        if(recordDttm.equals(glucose.getRecordDttm()))
            return true;
        else
            return false;
    }
}
