package control;

import entities.Filter;
import entities.Project;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ViewProjectFilter {
    public static List<Project> apply(List<Project> allProjects, Filter filter) {
        return allProjects.stream()
                .filter(filter::matches)
                .sorted(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}