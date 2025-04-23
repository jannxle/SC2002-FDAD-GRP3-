package control;

import entities.Filter;
import entities.Project;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class responsible for applying user-defined filters and sorting
 * to a list of BTO projects for display purposes.
 */
public class ViewProjectFilter {
    /**
     * Applies the specified filter criteria to a list of projects and sorts the result.
     * Filters projects based on the neighbourhood and room type specified in the Filter object.
     * Sorts the filtered list alphabetically by project name.
     *
     * @param allProjects The initial list of Project objects to be filtered.
     * @param filter      The Filter object containing the criteria to apply.
     * @return A new List containing only the projects that match the filter criteria,
     * sorted alphabetically by name. Returns an empty list if `allProjects` is null or empty.
     */
    public static List<Project> apply(List<Project> allProjects, Filter filter) {
        return allProjects.stream()
                .filter(filter::matches)
                .sorted(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}