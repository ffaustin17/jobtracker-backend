package com.ffaustin.job_tracker.specifications;

import com.ffaustin.job_tracker.dto.JobFilterRequest;
import com.ffaustin.job_tracker.entity.JobApplication;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobApplicationSpecification {

    public static Specification<JobApplication> withFilters(String userEmail, JobFilterRequest filter){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // join to access user's email
            Join<?, ?> userJoin = root.join("user");
            predicates.add(cb.equal(userJoin.get("email"), userEmail));

            if(filter.status() != null && !filter.status().isBlank()){
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }

            if(filter.company() != null && !filter.company().isBlank()){
                predicates.add(cb.equal(cb.lower(root.get("company")), "%" + filter.company().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
