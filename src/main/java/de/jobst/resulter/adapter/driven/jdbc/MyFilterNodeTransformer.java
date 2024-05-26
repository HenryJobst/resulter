package de.jobst.resulter.adapter.driven.jdbc;

import com.turkraft.springfilter.parser.node.*;
import com.turkraft.springfilter.transformer.FilterNodeTransformer;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyFilterNodeTransformer implements FilterNodeTransformer<FilterNodeTransformResult> {

    protected final ConversionService conversionService;

    public MyFilterNodeTransformer(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @NonNull
    @Override
    public Class<FilterNodeTransformResult> getTargetType() {
        return FilterNodeTransformResult.class;
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformField(FieldNode node) {
        return new FilterNodeTransformResult(node.getName());
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformInput(InputNode node) {
        return new FilterNodeTransformResult("'" + Objects.requireNonNull(conversionService.convert(node.getValue(),
            String.class), "Could not convert `" + node.getValue() + "` to string").replace("'", "\\'") + "'");
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformPriority(PriorityNode node) {
        FilterNodeTransformResult nodeTransform = transform(node.getNode());
        Map<String, String> filterMap = new HashMap<>(nodeTransform.filterMap());
        return new FilterNodeTransformResult("(" + nodeTransform + ")", filterMap);
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformPlaceholder(PlaceholderNode node) {
        return new FilterNodeTransformResult("`" + node.getPlaceholder().getName() + "`");
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformFunction(FunctionNode node) {
        return new FilterNodeTransformResult(node.getFunction().getName() + "(" + node.getArguments()
            .stream()
            .map(this::transform)
            .map(FilterNodeTransformResult::filterString)
            .collect(Collectors.joining(", ")) + ")");
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformCollection(CollectionNode node) {
        return new FilterNodeTransformResult("[" + node.getItems()
            .stream()
            .map(this::transform)
            .map(FilterNodeTransformResult::filterString)
            .collect(Collectors.joining(", ")) + "]");
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformPrefixOperation(PrefixOperationNode node) {
        FilterNodeTransformResult nodeTransform = transform(node.getRight());
        Map<String, String> filterMap = new HashMap<>(nodeTransform.filterMap());
        return new FilterNodeTransformResult(node.getOperator().getToken() + " " + nodeTransform, filterMap);
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformInfixOperation(InfixOperationNode node) {
        Map<String, String> filterMap = new HashMap<>();
        FilterNodeTransformResult leftTransform = transform(node.getLeft());
        FilterNodeTransformResult rightTransform = transform(node.getRight());
        filterMap.putAll(leftTransform.filterMap());
        filterMap.putAll(rightTransform.filterMap());
        if (node.getOperator().getToken().equals("~~")) {
            filterMap.put(leftTransform.filterString(), rightTransform.filterString());
        }
        return new FilterNodeTransformResult(leftTransform + " " + node.getOperator().getToken() + " " + rightTransform,
            filterMap);
    }

    @NonNull
    @Override
    public FilterNodeTransformResult transformPostfixOperation(PostfixOperationNode node) {
        return new FilterNodeTransformResult(transform(node.getLeft()) + " " + node.getOperator().getToken());
    }

}
